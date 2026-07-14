package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "EVE", collectorNumber = "48")
public class UmbraStalker extends Card {

    public UmbraStalker() {
        // Chroma — Power and toughness are each equal to the number of black mana symbols in the
        // mana costs of cards in your graveyard.
        ColorManaSymbolsInGraveyard blackSymbols =
                new ColorManaSymbolsInGraveyard(ManaColor.BLACK, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(blackSymbols, blackSymbols));
    }
}
