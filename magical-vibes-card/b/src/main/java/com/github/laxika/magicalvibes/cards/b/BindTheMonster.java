package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToControllerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "48")
public class BindTheMonster extends Card {

    public BindTheMonster() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new TapPermanentsEffect(Subjects.attached()),
                        TargetCreatureDealsDamageToControllerEffect.dealtBy(Subjects.attachedCreature(), new SourcePower(),
                                Subjects.you())))
                .addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
