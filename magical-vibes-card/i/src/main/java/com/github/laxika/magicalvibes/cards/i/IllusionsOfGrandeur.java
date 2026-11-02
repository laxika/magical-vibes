package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "79")
@CardRegistration(set = "ME1", collectorNumber = "40")
public class IllusionsOfGrandeur extends Card {

    public IllusionsOfGrandeur() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // When this enchantment enters, you gain 20 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(20));

        // When this enchantment leaves the battlefield, you lose 20 life.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new LoseLifeEffect(20));
    }
}
