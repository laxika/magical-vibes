package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "108")
@CardRegistration(set = "TLE", collectorNumber = "189")
public class LoAndLiRoyalAdvisors extends Card {

    public LoAndLiRoyalAdvisors() {
        PutCounterOnEachControlledPermanentEffect counterEffect =
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentHasSubtypePredicate(CardSubtype.ADVISOR));
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, counterEffect);
        addEffect(EffectSlot.ON_OPPONENT_MILLS, counterEffect);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U/B}",
                List.of(new MillEffect(4, MillRecipient.TARGET_PLAYER)),
                "{2}{U/B}: Target player mills four cards."
        ));
    }
}
