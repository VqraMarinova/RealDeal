package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.PictureSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Picture;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class PictureServiceImpl implements PictureService {
    private final PictureRepository pictureRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final CarRepository carRepository;

    public PictureServiceImpl(PictureRepository pictureRepository, ValidationUtil validationUtil, ModelMapper modelMapper, Gson gson, CarRepository carRepository) {
        this.pictureRepository = pictureRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.carRepository = carRepository;
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return Files.readString(Path.of(GlobalConstants.PICTURES_FILE_PATH));
    }

    @Override
    public String importPictures() throws IOException {
        StringBuilder result = new StringBuilder();
        PictureSeedDto[] pictureSeedDtos =  this.gson.fromJson(new FileReader(GlobalConstants.PICTURES_FILE_PATH), PictureSeedDto[].class);

        Arrays.stream(pictureSeedDtos).forEach(pictureSeedDto -> {
            if (this.validationUtil.isValid(pictureSeedDto)){
                if (this.pictureRepository.findByName(pictureSeedDto.getName()) != null){
                    result.append(GlobalConstants.DUPLICATE_MESSAGE + "picture").append(System.lineSeparator());;
                } else {
                    Picture picture = this.modelMapper.map(pictureSeedDto, Picture.class);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime registeredOn = LocalDateTime.parse(pictureSeedDto.getDateAndTime(), formatter);
                    picture.setDateAndTime(registeredOn);

                    Car car = this.carRepository.getOne(pictureSeedDto.getCar());
                    picture.setCar(car);

                    result.append(GlobalConstants.SUCCESSFUL_MESSAGE + "picture - " + picture.getName())
                            .append(System.lineSeparator());
                    this.pictureRepository.saveAndFlush(picture);

                }

            } else
                result.append(GlobalConstants.INCORRECT_MESSAGE + "picture").append(System.lineSeparator());;
        });


        return result.toString();
    }
}
