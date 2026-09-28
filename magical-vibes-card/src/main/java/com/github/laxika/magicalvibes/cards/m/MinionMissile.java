package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "663")
public class MinionMissile extends Card {

    public MinionMissile() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DiscardCardOrSacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
