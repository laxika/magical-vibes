package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCreatureWithLeastToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "308")
public class PurgingScythe extends Card {

    public PurgingScythe() {
        // At the beginning of your upkeep, this artifact deals 2 damage to the creature with the least toughness.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DealDamageToCreatureWithLeastToughnessEffect(2));
    }
}
