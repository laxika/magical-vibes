package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "15")
public class SpringjackShepherd extends Card {

    public SpringjackShepherd() {
        // Chroma — When this creature enters, create a 0/1 white Goat creature token for each
        // white mana symbol in the mana costs of permanents you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE),
                "Goat", 0, 1, CardColor.WHITE, List.of(CardSubtype.GOAT), Set.of(), Set.of()));
    }
}
