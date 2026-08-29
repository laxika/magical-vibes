package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DRK", collectorNumber = "43")
public class CurseArtifact extends Card {

    public CurseArtifact() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new MayEffect(
                                new SacrificeEnchantedCreatureEffect(),
                                "Sacrifice that artifact?",
                                new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER),
                                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
