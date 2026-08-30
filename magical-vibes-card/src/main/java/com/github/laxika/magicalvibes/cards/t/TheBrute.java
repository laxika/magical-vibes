package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "272")
@CardRegistration(set = "4ED", collectorNumber = "226")
@CardRegistration(set = "LEG", collectorNumber = "167")
public class TheBrute extends Card {

    public TheBrute() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}{R}",
                List.of(new RegenerateEffect()),
                "{R}{R}{R}: Regenerate enchanted creature."
        ));
    }
}
