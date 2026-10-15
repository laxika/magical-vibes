package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "93")
public class SporeBurst extends Card {

    public SporeBurst() {
        // Domain — Create a 1/1 green Saproling creature token for each basic land type among
        // lands you control.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new BasicLandTypesAmongControlledLands(),
                "Saproling", 1, 1, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
