package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.amount.TargetPower;

@CardRegistration(set = "RIX", collectorNumber = "103")
public class FormOfTheDinosaur extends Card {

    public FormOfTheDinosaur() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SetLifeTotalEffect(15));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.simultaneous(
                        DealDamageEffect.toTargetCreature(15),
                        DealDamageEffect.to(Subjects.you(), new TargetPower()).dealtBy(Subjects.target())));
    }
}
