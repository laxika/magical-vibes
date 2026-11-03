package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MagickedCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "73")
public class SidequestCardCollection extends Card {

    public SidequestCardCollection() {
        setBackFaceCard(new MagickedCard());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new DrawCardEffect(3),
                        new DiscardEffect(2, DiscardRecipient.CONTROLLER)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new GraveyardCardThreshold(8, null)),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "MagickedCard";
    }
}
