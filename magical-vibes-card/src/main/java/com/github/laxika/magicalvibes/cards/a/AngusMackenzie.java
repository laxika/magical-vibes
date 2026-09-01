package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "217")
public class AngusMackenzie extends Card {

    public AngusMackenzie() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{W}{U}",
                List.of(PreventDamageEffect.allCombat()),
                "{G}{W}{U}, {T}: Prevent all combat damage that would be dealt this turn. Activate only before the combat damage step.",
                ActivationTimingRestriction.BEFORE_COMBAT_DAMAGE
        ));
    }
}
