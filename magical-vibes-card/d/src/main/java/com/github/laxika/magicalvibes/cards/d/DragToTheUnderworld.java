package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THB", collectorNumber = "89")
public class DragToTheUnderworld extends Card {

    public DragToTheUnderworld() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK)));
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
