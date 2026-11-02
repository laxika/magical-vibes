package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "173")
@CardRegistration(set = "INR", collectorNumber = "402")
@CardRegistration(set = "EMN", collectorNumber = "146")
@CardRegistration(set = "SIR", collectorNumber = "179")
@CardRegistration(set = "C19", collectorNumber = "153")
@CardRegistration(set = "VOC", collectorNumber = "151")
public class StromkirkOccultist extends Card {

    public StromkirkOccultist() {
        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));

        // Whenever this creature deals combat damage to a player, exile the top card of your library.
        // Until end of turn, you may play that card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
