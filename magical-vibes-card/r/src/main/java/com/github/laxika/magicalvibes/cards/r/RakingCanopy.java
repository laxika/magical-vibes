package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "SHM", collectorNumber = "127")
public class RakingCanopy extends Card {

    public RakingCanopy() {
        // Whenever a creature with flying attacks you, this enchantment deals 4 damage to it.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new DealDamageToTriggeringAttackerEffect(4, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
