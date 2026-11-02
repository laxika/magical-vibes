package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "119")
public class PlagueSpitter extends Card {

    public PlagueSpitter() {
        // At the beginning of your upkeep, this creature deals 1 damage to each creature and each player.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MassDamageEffect(1, true));

        // When this creature dies, it deals 1 damage to each creature and each player.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MassDamageEffect(1, true));
    }
}
