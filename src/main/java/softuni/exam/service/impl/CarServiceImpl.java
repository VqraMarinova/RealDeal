package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.dom4j.Branch;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.agent.builder.AgentBuilder;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.CarSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.repository.CarRepository;
import softuni.exam.service.CarService;
import softuni.exam.util.ValidationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public CarServiceImpl(CarRepository carRepository, ValidationUtil validationUtil, ModelMapper modelMapper, Gson gson) {
        this.carRepository = carRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return this.carRepository.count() > 0;
    }

    @Override
    public String readCarsFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.CARS_FILE_PATH));
    }

    @Override
    public String importCars() throws IOException {
        StringBuilder result = new StringBuilder();
        CarSeedDto[] carSeedDtos =  this.gson.fromJson(new FileReader(GlobalConstants.CARS_FILE_PATH), CarSeedDto[].class);

        Arrays.stream(carSeedDtos).forEach(carSeedDto -> {
            if (this.validationUtil.isValid(carSeedDto)){
                if (this.carRepository.findByMakeAndModelAndKilometers(carSeedDto.getMake(), carSeedDto.getModel(), carSeedDto.getKilometers()) != null){
                    result.append(GlobalConstants.DUPLICATE_MESSAGE + "car").append(System.lineSeparator());;
                } else {
                    Car car = this.modelMapper.map(carSeedDto, Car.class);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate registeredOn = LocalDate.parse(carSeedDto.getRegisteredOn(), formatter);
                    car.setRegisteredOn(registeredOn);

                        result.append(GlobalConstants.SUCCESSFUL_MESSAGE + "car")
                                .append(String.format(" - %s - %s",car.getMake(), car.getModel()))
                                .append(System.lineSeparator());
                        this.carRepository.saveAndFlush(car);

                }

            } else
                result.append(GlobalConstants.INCORRECT_MESSAGE + "car").append(System.lineSeparator());;
        });


        return result.toString();
    }

    @Override
    public String getCarsOrderByPicturesCountThenByMake() {
        StringBuilder result = new StringBuilder();
        List<Car> cars = this.carRepository.allCarsOrderByPicturesCountDescAndMake();
        for (Car car : cars) {
            result.append(String.format("Car make - %s, model - %s", car.getMake(), car.getModel())).append(System.lineSeparator())
                    .append(String.format("\tKilometers - %d", car.getKilometers())).append(System.lineSeparator())
                    .append(String.format("\tRegistered on - %s", car.getRegisteredOn().toString())).append(System.lineSeparator())
                    .append(String.format("\tNumber of pictures - %d", car.getPictures().size())).append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        
        return result.toString();
    }
}
