package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

/**
 * Treasure Map — front face of Treasure Map // Treasure Cove.
 * {2} Artifact.
 * {1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or
 * more landmark counters on it, remove those counters, transform Treasure Map, and create
 * three Treasure tokens.
 */
@CardRegistration(set = "XLN", collectorNumber = "250")
@CardRegistration(set = "LCI", collectorNumber = "267")
@CardRegistration(set = "LCI", collectorNumber = "391")
public class TreasureMap extends Card {

    public TreasureMap() {
        setBackFaceCard(new TreasureCove());

        // {1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or
        // more landmark counters on it, remove those counters, transform Treasure Map, and create
        // three Treasure tokens.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(
                        new ScryEffect(1),
                        PutCountersEffect.onSelf(CounterType.LANDMARK),
                        new ConditionalEffect(new SourceCounterThreshold(3, CounterType.LANDMARK), SequenceEffect.of(
                                RemoveCountersEffect.allFromSelf(CounterType.LANDMARK),
                                new TransformSelfEffect(),
                                CreateTokenEffect.ofTreasureToken(3)))
                ),
                "{1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or more landmark counters on it, remove those counters, transform Treasure Map, and create three Treasure tokens."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TreasureCove";
    }
}
