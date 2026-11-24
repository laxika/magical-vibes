package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "78")
public class TemptingContract extends Card {

    public TemptingContract() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), TemptingOfferEffect.of(
                "Create a Treasure token? If you do, its controller creates a Treasure token.",
                CreateTokenEffect.ofTreasureToken(1)::forController));
    }
}
