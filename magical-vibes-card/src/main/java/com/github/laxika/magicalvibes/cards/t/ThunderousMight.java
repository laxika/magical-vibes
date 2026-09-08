package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "114")
public class ThunderousMight extends Card {

    public ThunderousMight() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK,
                new BoostEquippedCreatureUntilEndOfTurnEffect(
                        new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED),
                        new Fixed(0)));
    }
}
