package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "69")
public class LostHours extends Card {

    public LostHours() {
        addEffect(EffectSlot.SPELL,
                ChooseCardsFromTargetHandEffect.putIntoLibraryAtPosition(1, List.of(CardType.LAND), 2));
    }
}
