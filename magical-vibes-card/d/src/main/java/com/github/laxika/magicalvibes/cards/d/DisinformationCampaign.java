package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "167")
@CardRegistration(set = "MKC", collectorNumber = "208")
public class DisinformationCampaign extends Card {

    public DisinformationCampaign() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.youSurveil(), ReturnToHandEffect.self());
    }
}
