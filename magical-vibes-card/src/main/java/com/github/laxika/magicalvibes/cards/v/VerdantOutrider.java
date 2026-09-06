package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "196")
public class VerdantOutrider extends Card {

    public VerdantOutrider() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentPowerAtLeastPredicate(3),
                        "creatures with power 3 or greater",
                        true)),
                "{1}{G}: This creature can't be blocked by creatures with power 2 or less this turn."
        ));
    }
}
