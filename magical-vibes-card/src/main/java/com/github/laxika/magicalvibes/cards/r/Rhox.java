package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "291")
public class Rhox extends Card {

    public Rhox() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAsThoughUnblockedEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new RegenerateEffect()),
                false,
                "{2}{G}: Regenerate Rhox."
        ));
    }
}
