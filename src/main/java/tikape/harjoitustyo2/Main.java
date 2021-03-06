/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tikape.harjoitustyo2;

/**
 *
 * @author tire
 */
import java.util.HashMap;
import spark.ModelAndView;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.harjoitustyo2.database.Database;
import tikape.harjoitustyo2.domain.Kysymys;
import tikape.harjoitustyo2.domain.Vastaus;
import tikape.harjoitystyo2.dao.KysymysDao;
import tikape.harjoitystyo2.dao.VastausDao;

public class Main {


    public static void main(String[] args) throws Exception {

        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }

        Database database = new Database("jdbc:sqlite:tietokanta.db");
        KysymysDao kysymysDao = new KysymysDao(database);
        VastausDao vastausDao = new VastausDao(database);

        Spark.get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("kysymykset", kysymysDao.findAll());

            return new ModelAndView(map, "aloitussivu");
        }, new ThymeleafTemplateEngine());

        Spark.post("/lisaaKysymys", (req, res) -> {
            String kurssinNimi = req.queryParams("kurssinNimi");
            String aihe = req.queryParams("aihe");
            String kysymysteksti = req.queryParams("kysymysteksti");

            kysymysDao.saveOrUpdate(new Kysymys(-1, kurssinNimi, aihe, kysymysteksti));
            res.redirect("/");
            return "";
        });

        Spark.post("/lisaaVastaus", (req, res) -> {
            Integer kysymysId = Integer.parseInt(req.queryParams("kysymysId"));
            String vastausteksti = req.queryParams("vastausteksti");
            boolean oikein;
            if (req.queryParams("oikein") == null) {
                oikein = false;
            } else {
                oikein = true;
            }

            vastausDao.saveOrUpdate(new Vastaus(-1, kysymysId, vastausteksti, oikein));
            res.redirect("/");
            return "";
        });

        Spark.get("/kysymys/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            Integer kysymysId = Integer.parseInt(req.params(":id"));
            Kysymys k = kysymysDao.findOne(kysymysId);
            map.put("vastaukset", vastausDao.findAllForKysymys(k));
            map.put("kysymys", k);
            return new ModelAndView(map, "kysymys");
        }, new ThymeleafTemplateEngine());

        Spark.post("/poistaKysymys", (req, res) -> {
            Integer kysymysId = Integer.parseInt(req.queryParams("kysymysId"));
            vastausDao.deleteForKysymys(kysymysId);
            kysymysDao.delete(kysymysId);
            res.redirect("/");
            return "";
        });

        Spark.post("/poistaVastaus", (req, res) -> {
            Integer vastausId = Integer.parseInt(req.queryParams("vastausId"));
            vastausDao.delete(vastausId);
            res.redirect("/");
            return "";
        });

    }

}
