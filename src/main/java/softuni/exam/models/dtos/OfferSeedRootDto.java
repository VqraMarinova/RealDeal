package softuni.exam.models.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "offers")
@XmlAccessorType(XmlAccessType.FIELD)
public class OfferSeedRootDto {

    @XmlElement(name = "offer")
    private List<OfferSeedDto> offerSeedDtos;

    public OfferSeedRootDto() {
    }

    public List<OfferSeedDto> getOfferSeedDtos() {
        return offerSeedDtos;
    }

    public void setOfferSeedDtos(List<OfferSeedDto> offerSeedDtos) {
        this.offerSeedDtos = offerSeedDtos;
    }
}
