package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "4")
public class AnointedChorister extends Card {

    public AnointedChorister() {
        addActivatedAbility(new ActivatedAbility(false, "{4}{W}", List.of(new BoostSelfEffect(3, 3)),
                "{4}{W}: This creature gets +3/+3 until end of turn."));
    }
}
