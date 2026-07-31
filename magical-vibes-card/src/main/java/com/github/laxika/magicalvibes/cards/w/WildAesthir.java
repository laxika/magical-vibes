package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "21a")
@CardRegistration(set = "ALL", collectorNumber = "21b")
public class WildAesthir extends Card {

    public WildAesthir() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}", List.of(new BoostSelfEffect(2, 0)),
                "{W}{W}: Wild Aesthir gets +2/+0 until end of turn. Activate only once each turn.", 1));
    }
}
