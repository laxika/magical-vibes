package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "211")
public class Scald extends Card {

    public Scald() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new DealDamageOnLandTapEffect(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
