package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "248")
public class HotSprings extends Card {

    public HotSprings() {
        // Enchant land you control — grants the land "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null,
                                List.of(PreventDamageEffect.nextToAny(1)),
                                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
