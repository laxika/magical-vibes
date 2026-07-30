package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "INR", collectorNumber = "184")
@CardRegistration(set = "AVR", collectorNumber = "167")
public class AbundantGrowth extends Card {

    public AbundantGrowth() {
        // Enchant land — grants "{T}: Add one mana of any color."
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(),
                        GrantScope.ENCHANTED_PERMANENT
                ))
                // When this Aura enters, draw a card.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
    }
}
