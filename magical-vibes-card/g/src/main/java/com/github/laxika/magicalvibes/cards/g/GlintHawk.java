package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "10")
public class GlintHawk extends Card {

    public GlintHawk() {
        // When this creature enters, sacrifice it unless you return an artifact you control to its owner's hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(new PermanentIsArtifactPredicate(), "artifact you control"),
                new SacrificeSelfEffect()));
    }
}
