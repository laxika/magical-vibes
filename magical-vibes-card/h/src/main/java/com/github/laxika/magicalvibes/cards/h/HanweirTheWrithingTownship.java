package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

/**
 * Hanweir, the Writhing Township — meld result of Hanweir Battlements and Hanweir Garrison.
 * Trample and Haste are loaded from Scryfall.
 */
@CardRegistration(set = "INR", collectorNumber = "157b")
public class HanweirTheWrithingTownship extends Card {

    public HanweirTheWrithingTownship() {
        // Whenever this creature attacks, create two 3/2 colorless Eldrazi Horror creature tokens
        // that are tapped and attacking.
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                2, "Eldrazi Horror", 3, 2, null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), true
        ));
    }
}
