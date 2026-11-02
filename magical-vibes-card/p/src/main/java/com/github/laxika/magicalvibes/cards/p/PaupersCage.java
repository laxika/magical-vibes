package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "314")
public class PaupersCage extends Card {

    public PaupersCage() {
        // "At the beginning of each opponent's upkeep, if that player has two or fewer cards in
        // hand, this artifact deals 2 damage to that player."
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new DealDamageIfFewCardsInHandEffect(2, 2));
    }
}
