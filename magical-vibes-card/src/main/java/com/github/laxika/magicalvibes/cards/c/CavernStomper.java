package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "177")
public class CavernStomper extends Card {

    public CavernStomper() {
        // When this creature enters, scry 2.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        // {3}{G}: This creature can't be blocked by creatures with power 2 or less this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentPowerAtLeastPredicate(3),
                        "creatures with power 3 or greater",
                        true)),
                "{3}{G}: This creature can't be blocked by creatures with power 2 or less this turn."
        ));
    }
}
