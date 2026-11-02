package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "227")
public class AetherRift extends Card {

    public AetherRift() {
        // At the beginning of your upkeep, discard a card at random. If you discard a creature card
        // this way, return it from your graveyard to the battlefield unless any player pays 5 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(5));
    }
}
