package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.InheritedFiend;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;

import java.util.List;

/**
 * Heirloom Mirror's front face.
 */
@CardRegistration(set = "MID", collectorNumber = "105")
public class HeirloomMirror extends Card {

    public HeirloomMirror() {
        setBackFaceCard(new InheritedFiend());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new PayLifeCost(1),
                        new DiscardCardTypeCost(null, null),
                        new DrawCardEffect(1),
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new PutCounterOnSelfThenTransformIfThresholdEffect(CounterType.RITUAL, 3)
                ),
                "{1}, {T}, Pay 1 life, Discard a card: Draw a card, mill a card, then put a ritual counter on this artifact. Then if it has three or more ritual counters on it, remove them and transform it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "InheritedFiend";
    }
}
