package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.OfferSeedRootDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Offer;
import softuni.exam.models.entities.Picture;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.OfferRepository;
import softuni.exam.repository.PictureRepository;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.OfferService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;


@Service
public class OfferServiceImpl implements OfferService {
    private final OfferRepository offerRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final CarRepository carRepository;
    private final SellerRepository sellerRepository;
    private final PictureRepository pictureRepository;

    public OfferServiceImpl(OfferRepository offerRepository, ValidationUtil validationUtil, ModelMapper modelMapper, XmlParser xmlParser, CarRepository carRepository, SellerRepository sellerRepository, PictureRepository pictureRepository) {
        this.offerRepository = offerRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.carRepository = carRepository;
        this.sellerRepository = sellerRepository;
        this.pictureRepository = pictureRepository;
    }

    @Override
    public boolean areImported() {
        return this.offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.OFFERS_FILE_PATH));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder result = new StringBuilder();
        OfferSeedRootDto offerSeedRootDto = this.xmlParser.parseXml(OfferSeedRootDto.class,GlobalConstants.OFFERS_FILE_PATH);

        offerSeedRootDto.getOfferSeedDtos().forEach(offerSeedDto -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime addedOn = LocalDateTime.parse(offerSeedDto.getAddedOn(), formatter);

                if (this.validationUtil.isValid(offerSeedDto)){
                    if (this.offerRepository.findByDescriptionAndAddedOn(offerSeedDto.getDescription(),addedOn ) != null){
                        result.append(GlobalConstants.DUPLICATE_MESSAGE + "offer").append(System.lineSeparator());;
                    } else {
                        Offer offer = this.modelMapper.map(offerSeedDto, Offer.class);


                        offer.setAddedOn(addedOn);

                        Car car = this.carRepository.getOne(offerSeedDto.getCar().getId());
                        offer.setCar(car);

                        Seller seller = this.sellerRepository.getOne(offerSeedDto.getSeller().getId());
                        offer.setSeller(seller);

                       Set<Picture> pictures = this.pictureRepository.getAllByCar(car);
                       offer.setPictures(pictures);


                        result.append(GlobalConstants.SUCCESSFUL_MESSAGE + "offer - " + offer.getAddedOn() + " - " + offer.isHasGoldStatus())
                                .append(System.lineSeparator());
                        this.offerRepository.saveAndFlush(offer);

                    }

                } else
                    result.append(GlobalConstants.INCORRECT_MESSAGE + "offer").append(System.lineSeparator());;
            });


            return result.toString();
    }
}
