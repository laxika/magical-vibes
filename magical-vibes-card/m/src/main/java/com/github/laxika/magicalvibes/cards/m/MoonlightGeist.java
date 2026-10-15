package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "29")
public class MoonlightGeist extends Card {

    public MoonlightGeist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(
                        PreventDamageEffect.allCombatToSelf(),
                        PreventDamageEffect.allCombatBySelf()
                ),
                "{3}{W}: Prevent all combat damage that would be dealt to and dealt by this creature this turn."
        ));
    }
}
