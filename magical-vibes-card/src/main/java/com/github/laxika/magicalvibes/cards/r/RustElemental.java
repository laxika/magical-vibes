package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "234")
public class RustElemental extends Card {

    public RustElemental() {
        // Flying is a printed keyword loaded from Scryfall.
        // At the beginning of your upkeep, sacrifice another artifact. If you can't, tap this
        // creature and you lose 4 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                List.of(new TapPermanentsEffect(TapUntapScope.SELF), new LoseLifeEffect(4))));
    }
}
