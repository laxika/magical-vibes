package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "38")
public class ValiantVeteran extends Card {

    public ValiantVeteran() {
        // Other Soldiers you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)));

        // {3}{W}{W}, Exile this card from your graveyard: Put a +1/+1 counter on each Soldier you control.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                1,
                                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER))
                ),
                "{3}{W}{W}, Exile this card from your graveyard: Put a +1/+1 counter on each Soldier you control."
        ));
    }
}
