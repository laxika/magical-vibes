package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "CON", collectorNumber = "46")
@CardRegistration(set = "MM3", collectorNumber = "74")
public class GrixisSlavedriver extends Card {

    public GrixisSlavedriver() {
        // When this creature leaves the battlefield, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), CreateTokenEffect.blackZombie(1));

        // Unearth {3}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{3}{B}");
    }
}
