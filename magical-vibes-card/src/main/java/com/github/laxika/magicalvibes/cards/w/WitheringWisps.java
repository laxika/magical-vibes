package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "168")
public class WitheringWisps extends Card {

    public WitheringWisps() {
        // At the beginning of the end step, if no creatures are on the battlefield, sacrifice this enchantment.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                new SacrificeSelfEffect()));

        // {B}: This enchantment deals 1 damage to each creature and each player.
        // Activate no more times each turn than the number of snow Swamps you control.
        var snowSwamp = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
        ));

        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new MassDamageEffect(1, true)),
                "{B}: This enchantment deals 1 damage to each creature and each player. "
                        + "Activate no more times each turn than the number of snow Swamps you control.")
                .withMaxActivationsPerTurn(
                        new PermanentCount(snowSwamp, CountScope.CONTROLLER),
                        "the number of snow Swamps you control"));
    }
}
