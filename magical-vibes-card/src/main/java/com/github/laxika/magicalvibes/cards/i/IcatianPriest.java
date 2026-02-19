package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "24")
public class IcatianPriest extends Card {

    public IcatianPriest() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{W}", List.of(new BoostTargetCreatureEffect(1, 1)), true, "{1}{W}{W}: Target creature gets +1/+1 until end of turn."));
    }
}
