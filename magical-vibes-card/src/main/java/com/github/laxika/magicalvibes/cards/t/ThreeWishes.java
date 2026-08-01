package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect;

@CardRegistration(set = "VIS", collectorNumber = "45")
public class ThreeWishes extends Card {

    public ThreeWishes() {
        // Exile the top three cards of your library face down. You may look at those cards for as
        // long as they remain exiled. Until your next turn, you may play those cards. At the
        // beginning of your next upkeep, put any of those cards you didn't play into your graveyard.
        // Ruling: play window closes at the start of your next upkeep (same cleanup as Grinning Totem).
        addEffect(EffectSlot.SPELL, new ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect(3));
    }
}
