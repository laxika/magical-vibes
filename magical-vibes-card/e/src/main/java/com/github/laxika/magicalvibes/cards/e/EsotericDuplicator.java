package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "5")
public class EsotericDuplicator extends Card {

    public EsotericDuplicator() {
        MayPayManaEffect delayedCopy = new MayPayManaEffect(
                "{2}", new RegisterDelayedTokenCopyEffect(),
                "Pay {2} to create a token that's a copy of that artifact?");

        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.sacrificed(new PermanentIsArtifactPredicate(),
                                ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.sacrificed(ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(delayedCopy))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this artifact: Draw a card."
        ));
    }
}
