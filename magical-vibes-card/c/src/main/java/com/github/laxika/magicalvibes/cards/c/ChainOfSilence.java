package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerSacrificeThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "12")
public class ChainOfSilence extends Card {

    public ChainOfSilence() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allByTargetCreatures())
                .addEffect(EffectSlot.SPELL, new MayEffect(
                        new TargetPermanentControllerSacrificeThenEffect(
                                new PermanentIsLandPredicate(),
                                new MayEffect(
                                        new CopyThisSpellForTargetControllerEffect(),
                                        "Copy this spell?",
                                        null,
                                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER),
                                "a land"),
                        "Sacrifice a land?",
                        null,
                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER));
    }
}
