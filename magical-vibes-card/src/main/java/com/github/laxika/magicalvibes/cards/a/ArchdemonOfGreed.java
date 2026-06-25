package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapSelfEffect;

import java.util.List;

/**
 * Archdemon of Greed - back face of Ravenous Demon.
 */
public class ArchdemonOfGreed extends Card {

    public ArchdemonOfGreed() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificeSubtypeCreatureCost(CardSubtype.HUMAN),
                        List.of(new TapSelfEffect(), new DealDamageToControllerEffect(9))));
    }
}
