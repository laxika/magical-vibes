package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "12")
public class ChainOfSilence extends Card {

    public ChainOfSilence() {
        target(TargetFilters.creature())
                .addSpellEffect(PreventDamageEffect.allByTargetCreatures())
                .addSpellEffect(new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                                        Subjects.controllerOf(Subjects.target())),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new MayEffect(
                                        new CopyThisSpellForTargetControllerEffect(),
                                        "Copy this spell?",
                                        null,
                                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER))),
                        "Sacrifice a land?",
                        null,
                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER));
    }
}
