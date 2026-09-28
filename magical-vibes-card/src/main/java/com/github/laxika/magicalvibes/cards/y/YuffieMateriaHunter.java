package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfControlledEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "65")
@CardRegistration(set = "FIC", collectorNumber = "158")
public class YuffieMateriaHunter extends Card {

    public YuffieMateriaHunter() {
        addNinjutsu("{1}{R}");

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                "Target must be a noncreature artifact"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new AttachOneOfControlledEquipmentToSourceEffect(),
                        "Attach an Equipment you control to Yuffie?"));
    }
}
