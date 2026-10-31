package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "ORI", collectorNumber = "114")
@CardRegistration(set = "THS", collectorNumber = "101")
@CardRegistration(set = "DDP", collectorNumber = "56")
@CardRegistration(set = "TSR", collectorNumber = "328")
@CardRegistration(set = "PIO", collectorNumber = "104")
@CardRegistration(set = "C14", collectorNumber = "158")
@CardRegistration(set = "CMM", collectorNumber = "182")
@CardRegistration(set = "MOC", collectorNumber = "267")
@CardRegistration(set = "DSC", collectorNumber = "154")
@CardRegistration(set = "DMC", collectorNumber = "117")
@CardRegistration(set = "C17", collectorNumber = "122")
@CardRegistration(set = "SCD", collectorNumber = "101")
public class ReadTheBones extends Card {

    public ReadTheBones() {
        addSpellEffect(new ScryEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
    }
}
