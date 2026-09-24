package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "AA3", collectorNumber = "21")
@CardRegistration(set = "MH2", collectorNumber = "227")
public class KaldraCompleat extends Card {

    public KaldraCompleat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(5, 5, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE),
                GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new ExileTargetPermanentEffect(), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{7}"));
    }
}
