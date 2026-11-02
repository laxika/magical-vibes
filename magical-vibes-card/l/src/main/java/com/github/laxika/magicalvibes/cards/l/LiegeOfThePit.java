package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "113")
public class LiegeOfThePit extends Card {

    public LiegeOfThePit() {
        addMorph("{B}{B}{B}{B}");
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificeOtherCreatureOrDamageEffect(7));
    }
}
