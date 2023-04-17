package academy.prog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// DB -> E(20) -> R -> S -> DTO <- C -> View / JSON (5)

@Service
public class UrlService {
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public long saveUrl(UrlDTO urlDTO) {
        var urlRecord = urlRepository.findByUrl(urlDTO.getUrl());
        if (urlRecord == null) {
            urlRecord = UrlRecord.of(urlDTO);
            urlRepository.save(urlRecord);
        }

        return urlRecord.getId();
    }

    @Transactional
    public String getUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty())
            return null;

        var urlRecord = urlOpt.get();
        urlRecord.setCount(urlRecord.getCount() + 1);
        urlRecord.setLastAccess(new Date());

        return urlRecord.getUrl();
    }

    @Transactional(readOnly = true)
    public List<UrlStatDTO> getStatistics() {
        var records = urlRepository.findAll();
        var result = new ArrayList<UrlStatDTO>();

        records.forEach(x -> result.add(x.toStatDTO()));

        return result;
    }

    @Transactional
    public String delUrl(long id) {
        var urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) {
            String err = "i can`t find this URL";
            return err;
        } else {
            urlRepository.deleteById(id);
            String rez = id + " is deleted";
            return rez;
        }
    }

    public List<UrlStatDTO> delOldLink() {
        var records = urlRepository.findAll();
        var result = new ArrayList<UrlStatDTO>();
        UrlStatDTO stat = new UrlStatDTO();
        stat.setMessage(" 0i dont have old links");
        Date today = new Date();
        for (UrlRecord urlRecor: records) {
            int raz = today.getDay() - urlRecor.getLastAccess().getDay();
            if (raz >= 1) {
                delUrl(urlRecor.getId());
                result.add(urlRecor.toStatDTO());
                return result;
            }
        }
        result.add(stat);
        return result;
    }
}
