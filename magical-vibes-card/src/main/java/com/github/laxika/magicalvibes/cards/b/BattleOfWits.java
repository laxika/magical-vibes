package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "9ED", collectorNumber = "65")
public class BattleOfWits extends Card {

    public BattleOfWits() {
        // At the beginning of your upkeep, if you have 200 or more cards in your library, you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new CardsInLibraryAtLeast(200), new WinGameEffect()));
    }
}
