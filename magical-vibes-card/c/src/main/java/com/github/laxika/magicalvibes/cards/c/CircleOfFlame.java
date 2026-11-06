package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "127")
@CardRegistration(set = "M15", collectorNumber = "135")
public class CircleOfFlame extends Card {

    public CircleOfFlame() {
        // Whenever a creature without flying attacks you or a planeswalker you control,
        // this enchantment deals 1 damage to that creature.
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker()
                        .withFilter(new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))),
                new DealDamageToTriggeringAttackerEffect(1));
    }
}
