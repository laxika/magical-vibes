package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THB", collectorNumber = "86")
public class BlightBreathCatoblepas extends Card {

    public BlightBreathCatoblepas() {
        ColorManaSymbolsAmongControlledPermanents blackDevotion =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK);
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostTargetCreatureEffect(new Scaled(blackDevotion, -1), new Scaled(blackDevotion, -1)));
    }
}
