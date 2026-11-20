package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "53")
public class RingingStrikeMastery extends Card {

    public RingingStrikeMastery() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(Subjects.attached()))
                .addStaticEffect(DoesntUntapEffect.enchanted())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{5}",
                                List.of(new UntapPermanentsEffect(Subjects.source())),
                                "{5}: Untap this creature."
                        ),
                        Subjects.attachedCreature()
                ));
    }
}
