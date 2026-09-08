package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "187")
public class UnlicensedDisintegration extends Card {

    public UnlicensedDisintegration() {
        // Destroy target creature. If you control an artifact, Unlicensed Disintegration deals 3
        // damage to that creature's controller.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new ConditionalEffect(
                        new ControlsPermanent(new PermanentIsArtifactPredicate()),
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER)),
                ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
