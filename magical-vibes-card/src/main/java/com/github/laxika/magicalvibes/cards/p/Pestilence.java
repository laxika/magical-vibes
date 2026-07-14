package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "149")
public class Pestilence extends Card {

    public Pestilence() {
        // At the beginning of the end step, if no creatures are on the battlefield, sacrifice this enchantment.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate()),
                new SacrificeSelfEffect()));

        // {B}: This enchantment deals 1 damage to each creature and each player.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new MassDamageEffect(1, true)),
                "{B}: This enchantment deals 1 damage to each creature and each player."));
    }
}
