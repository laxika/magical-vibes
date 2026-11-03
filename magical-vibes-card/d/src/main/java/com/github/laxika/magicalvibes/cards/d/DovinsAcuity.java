package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "168")
public class DovinsAcuity extends Card {

    public DovinsAcuity() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.INSTANT)),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerMainPhase()),
                new MayEffect(ReturnToHandEffect.self(), "Return Dovin's Acuity to its owner's hand?"));
    }
}
