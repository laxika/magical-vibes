package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "48")
public class EscapeProtocol extends Card {

    public EscapeProtocol() {
        PermanentPredicate targetArtifactOrCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                ))
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new CyclingTriggerEffect(new MayPayManaEffect(
                        "{1}",
                        new QueueReflexiveAbilityEffect(
                                FlickerEffect.flickerTarget(targetArtifactOrCreatureYouControl)),
                        "Pay {1} to exile an artifact or creature you control?")));
    }
}
