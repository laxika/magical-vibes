package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RashmiRevealTopCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "184")
@CardRegistration(set = "KLR", collectorNumber = "202")
@CardRegistration(set = "DSC", collectorNumber = "231")
@CardRegistration(set = "C20", collectorNumber = "229")
public class RashmiEternitiesCrafter extends Card {

    public RashmiEternitiesCrafter() {
        // Whenever you cast your first spell each turn, reveal the top card of your library. If it's a nonland
        // card with mana value less than that spell's, you may cast it without paying its mana cost. If you don't
        // cast the revealed card, put it into your hand.
        addTrigger(Triggers.youCastSpell().nthThisTurn(1), new RashmiRevealTopCardEffect());
    }
}
