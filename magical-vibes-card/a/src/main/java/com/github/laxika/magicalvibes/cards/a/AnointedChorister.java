package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "4")
public class AnointedChorister extends Card {

    public AnointedChorister() {
        addActivatedAbility(new ActivatedAbility(false, "{4}{W}", List.of(BoostEffect.self(3, 3)),
                "{4}{W}: This creature gets +3/+3 until end of turn."));
    }
}
