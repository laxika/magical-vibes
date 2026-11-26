package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "211")
public class Scald extends Card {

    public Scald() {
        // Whenever a player taps an Island for mana, Scald deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));
    }
}
