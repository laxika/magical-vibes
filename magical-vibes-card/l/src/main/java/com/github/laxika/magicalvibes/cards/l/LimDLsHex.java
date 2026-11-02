package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "146")
public class LimDLsHex extends Card {

    public LimDLsHex() {
        // At the beginning of your upkeep, for each player, this enchantment deals 1 damage to that
        // player unless they pay {B} or {3}. Monocolored hybrid {B/3} is the payable form of that choice.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new EachPlayerTakesDamageUnlessPaysEffect(1, "{B/3}"));
    }
}
