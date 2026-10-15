package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "EVE", collectorNumber = "73")
public class Primalcrux extends Card {

    public Primalcrux() {
        // Chroma — Power and toughness are each equal to the number of green mana symbols in the
        // mana costs of permanents you control (Primalcrux itself included). Trample is auto-loaded.
        ColorManaSymbolsAmongControlledPermanents greenSymbols =
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(greenSymbols, greenSymbols));
    }
}
