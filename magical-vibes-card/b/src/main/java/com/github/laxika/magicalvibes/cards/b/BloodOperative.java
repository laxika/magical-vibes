package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "63")
public class BloodOperative extends Card {

    public BloodOperative() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                "You may exile target card from a graveyard."
        ));
        addTrigger(new TriggerCondition(Triggers.youSurveil(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new ConditionalEffect(new SourceCardInGraveyard(),
                        new MayPayManaEffect("{0}", 3,
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay 3 life to return Blood Operative from your graveyard to your hand?")));
    }
}
