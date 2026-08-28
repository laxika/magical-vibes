package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "THB", collectorNumber = "176")
public class KlothyssDesign extends Card {

    public KlothyssDesign() {
        ColorManaSymbolsAmongControlledPermanents greenDevotion =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN);
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(greenDevotion, greenDevotion));
    }
}
