package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "252")
public class LlanowarBehemoth extends Card {

    public LlanowarBehemoth() {
        // Tap an untapped creature you control: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        new BoostSelfEffect(1, 1)),
                "Tap an untapped creature you control: This creature gets +1/+1 until end of turn."
        ));
    }
}
