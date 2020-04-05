package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.OfferSeedRootDto;
import softuni.exam.models.dtos.SellerSeedRootDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Offer;
import softuni.exam.models.entities.Rating;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;

    public SellerServiceImpl(SellerRepository sellerRepository, ValidationUtil validationUtil, ModelMapper modelMapper, XmlParser xmlParser) {
        this.sellerRepository = sellerRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return this.sellerRepository.count() > 0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        return Files.readString(Path.of(GlobalConstants.SELLERS_FILE_PATH));
    }

    @Override
    public String importSellers() throws IOException, JAXBException {
        StringBuilder result = new StringBuilder();
        SellerSeedRootDto sellerSeedRootDto = this.xmlParser.parseXml(SellerSeedRootDto.class,GlobalConstants.SELLERS_FILE_PATH);

        sellerSeedRootDto.getSellerSeedDtos().forEach(sellerSeedDto -> {

            if (this.validationUtil.isValid(sellerSeedDto)){
                if (this.sellerRepository.findByFirstNameAndLastNameAndEmail(sellerSeedDto.getFirstName(),
                        sellerSeedDto.getLastName(), sellerSeedDto.getEmail()) != null){
                    result.append(GlobalConstants.DUPLICATE_MESSAGE + "seller").append(System.lineSeparator());;
                } else {
                    Seller seller = this.modelMapper.map(sellerSeedDto, Seller.class);



                    result.append(GlobalConstants.SUCCESSFUL_MESSAGE + "seller - " + seller.getLastName() + " - " + seller.getEmail())
                            .append(System.lineSeparator());
                    this.sellerRepository.saveAndFlush(seller);

                }

            } else
                result.append(GlobalConstants.INCORRECT_MESSAGE + "seller").append(System.lineSeparator());;
        });


        return result.toString();
    }
}
