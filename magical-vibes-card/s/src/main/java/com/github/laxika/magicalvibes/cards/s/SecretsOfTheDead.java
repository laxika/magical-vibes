package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "48")
@CardRegistration(set = "C19", collectorNumber = "95")
public class SecretsOfTheDead extends Card {

    public SecretsOfTheDead() {
        // Whenever you cast a spell from your graveyard, draw a card.
        addTrigger(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), new DrawCardEffect(1));
    }
}
