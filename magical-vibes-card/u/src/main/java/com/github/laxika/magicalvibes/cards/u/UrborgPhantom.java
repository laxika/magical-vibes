package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "132")
public class UrborgPhantom extends Card {

    public UrborgPhantom() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        PreventDamageEffect.allCombatToSelf(),
                        PreventDamageEffect.allCombatBySelf()
                ),
                "{U}: Prevent all combat damage that would be dealt to and dealt by this creature this turn."
        ));
    }
}
