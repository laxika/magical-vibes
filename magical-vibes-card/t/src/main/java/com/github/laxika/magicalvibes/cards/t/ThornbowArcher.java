package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "121")
public class ThornbowArcher extends Card {

    public ThornbowArcher() {
        // Whenever this creature attacks, each opponent who doesn't control an Elf loses 1 life.
        addTrigger(Triggers.attacks(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT,
                new PermanentHasSubtypePredicate(CardSubtype.ELF)));
    }
}
