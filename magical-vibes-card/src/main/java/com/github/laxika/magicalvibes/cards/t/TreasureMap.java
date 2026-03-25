package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

/**
 * Treasure Map — front face of Treasure Map // Treasure Cove.
 * {2} Artifact.
 * {1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or
 * more landmark counters on it, remove those counters, transform Treasure Map, and create
 * three Treasure tokens.
 */
@CardRegistration(set = "XLN", collectorNumber = "250")
public class TreasureMap extends Card {

    public TreasureMap() {
        // Set up back face
        TreasureCove backFace = new TreasureCove();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or
        // more landmark counters on it, remove those counters, transform Treasure Map, and create
        // three Treasure tokens.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(
                        new ScryEffect(1),
                        new PutCounterOnSelfThenTransformIfThresholdEffect(
                                CounterType.LANDMARK, 3, false,
                                List.of(CreateTokenEffect.ofTreasureToken(3))
                        )
                ),
                "{1}, {T}: Scry 1. Put a landmark counter on Treasure Map. Then if there are three or more landmark counters on it, remove those counters, transform Treasure Map, and create three Treasure tokens."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TreasureCove";
    }
}
