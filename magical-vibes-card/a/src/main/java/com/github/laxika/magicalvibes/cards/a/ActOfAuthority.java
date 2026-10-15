package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "1")
public class ActOfAuthority extends Card {

    public ActOfAuthority() {
        PermanentPredicateTargetFilter artifactOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        );

        target(artifactOrEnchantment)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new ExileTargetPermanentEffect(),
                                "Exile target artifact or enchantment?"))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new MayEffect(SequenceEffect.of(
                                new TargetPermanentControllerGainsControlOfSourceEffect(ControlDuration.PERMANENT),
                                new ExileTargetPermanentEffect()
                        ), "Exile target artifact or enchantment?"));
    }
}
