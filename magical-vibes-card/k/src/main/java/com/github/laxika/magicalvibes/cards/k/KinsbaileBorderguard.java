package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "14")
public class KinsbaileBorderguard extends Card {

    public KinsbaileBorderguard() {
        // Kinsbaile Borderguard enters the battlefield with a +1/+1 counter on it for each other
        // Kithkin you control. (It isn't on the battlefield yet while the amount is evaluated, so
        // the battlefield count naturally covers only the *other* Kithkin.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.KITHKIN), CountScope.CONTROLLER)));

        // When Kinsbaile Borderguard dies, create a 1/1 white Kithkin Soldier creature token for
        // each counter on it.
        addEffect(EffectSlot.ON_DEATH, new CreateTokensForEachDyingSourceCounterEffect(
                new CreateTokenEffect("Kithkin Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
