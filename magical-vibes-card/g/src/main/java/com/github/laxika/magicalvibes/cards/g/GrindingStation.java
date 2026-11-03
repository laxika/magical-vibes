package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "127")
@CardRegistration(set = "MB2", collectorNumber = "223")
public class GrindingStation extends Card {

    public GrindingStation() {
        // {T}, Sacrifice an artifact: Target player mills three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new MillEffect(3, MillRecipient.TARGET_PLAYER)
                ),
                "{T}, Sacrifice an artifact: Target player mills three cards."
        ));

        // Whenever an artifact enters, you may untap Grinding Station.
        addTrigger(Triggers.enters(new PermanentIsArtifactPredicate(), ObjectRelation.ANY),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap Grinding Station?"));
    }
}
