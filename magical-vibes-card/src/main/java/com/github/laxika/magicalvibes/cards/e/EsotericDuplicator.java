package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "5")
public class EsotericDuplicator extends Card {

    public EsotericDuplicator() {
        MayPayManaEffect delayedCopy = new MayPayManaEffect(
                "{2}", new RegisterDelayedTokenCopyEffect(),
                "Pay {2} to create a token that's a copy of that artifact?");

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(new PermanentIsArtifactPredicate(), delayedCopy));
        addEffect(EffectSlot.ON_DEATH, delayedCopy);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this artifact: Draw a card."
        ));
    }
}
