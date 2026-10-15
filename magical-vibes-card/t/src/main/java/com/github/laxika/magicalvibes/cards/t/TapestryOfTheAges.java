package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "246")
public class TapestryOfTheAges extends Card {

    public TapestryOfTheAges() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1)),
                "{2}, {T}: Draw a card. Activate only if you've cast a noncreature spell this turn.",
                ActivationTimingRestriction.CAST_NONCREATURE_SPELL_THIS_TURN));
    }
}
