package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "222")
public class WitchesEye extends Card {

    public WitchesEye() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(new ScryEffect(1)),
                        "{1}, {T}: Scry 1."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
