package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TotalCreatureCardsInGraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "56")
@CardRegistration(set = "TSB", collectorNumber = "37")
public class AvatarOfWoe extends Card {

    public AvatarOfWoe() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new TotalCreatureCardsInGraveyardsAtLeast(10), new ReduceOwnCastCostEffect(new Fixed(6))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(true)),
                "{T}: Destroy target creature. It can't be regenerated.",
                TargetFilters.creature()
        ));
    }
}
