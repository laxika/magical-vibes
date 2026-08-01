package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "83")
public class UnderworldConnections extends Card {

    public UnderworldConnections() {
        // Enchant land — the enchanted land gains "{T}, Pay 1 life: Draw a card."
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null,
                                List.of(new PayLifeCost(1), new DrawCardEffect(1)),
                                "{T}, Pay 1 life: Draw a card."),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
