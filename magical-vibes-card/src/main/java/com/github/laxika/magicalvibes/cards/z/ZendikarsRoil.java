package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "209")
public class ZendikarsRoil extends Card {

    public ZendikarsRoil() {
        // Landfall — Whenever a land you control enters, create a 2/2 green Elemental creature token.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Elemental", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.ELEMENTAL),
                        Set.of(), Set.of()));
    }
}
