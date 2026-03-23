package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "238")
public class FellFlagship extends Card {

    public FellFlagship() {
        // Pirates you control get +1/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PIRATE))));

        // Whenever Fell Flagship deals combat damage to a player, that player discards a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerDiscardsEffect(1));

        // Crew 3
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), new AnimateSelfAsCreatureEffect()),
                "Crew 3"
        ));
    }
}
