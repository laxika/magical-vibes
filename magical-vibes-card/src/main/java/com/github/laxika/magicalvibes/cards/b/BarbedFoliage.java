package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "MIR", collectorNumber = "207")
public class BarbedFoliage extends Card {

    public BarbedFoliage() {
        // Whenever a creature attacks you, it loses flanking until end of turn.
        // The trigger's non-targeting targetId is the attacking creature, so GrantScope.TARGET
        // strips flanking from it without any target selection.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new RemoveKeywordEffect(Keyword.FLANKING, GrantScope.TARGET));

        // Whenever a creature without flying attacks you, this enchantment deals 1 damage to it.
        PermanentPredicate withoutFlying = new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new DealDamageToTriggeringAttackerEffect(1, withoutFlying));
    }
}
