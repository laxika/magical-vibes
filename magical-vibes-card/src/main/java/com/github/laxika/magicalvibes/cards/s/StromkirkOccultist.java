package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "INR", collectorNumber = "173")
public class StromkirkOccultist extends Card {

    public StromkirkOccultist() {
        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));

        // Whenever this creature deals combat damage to a player, exile the top card of your library.
        // Until end of turn, you may play that card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
