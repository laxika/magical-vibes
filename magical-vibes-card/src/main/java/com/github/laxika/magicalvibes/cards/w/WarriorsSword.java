package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "169")
public class WarriorsSword extends Card {

    public WarriorsSword() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LivingWeaponEffect(new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of())));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.WARRIOR, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
