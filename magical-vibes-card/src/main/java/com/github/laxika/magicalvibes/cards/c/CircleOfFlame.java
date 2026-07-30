package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M12", collectorNumber = "127")
public class CircleOfFlame extends Card {

    public CircleOfFlame() {
        // Whenever a creature without flying attacks you or a planeswalker you control,
        // this enchantment deals 1 damage to that creature.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new DealDamageToTriggeringAttackerEffect(1,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
