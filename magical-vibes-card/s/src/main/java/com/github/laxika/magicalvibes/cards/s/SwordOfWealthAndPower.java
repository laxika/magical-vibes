package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "26")
public class SwordOfWealthAndPower extends Card {

    public SwordOfWealthAndPower() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantEffectEffect(
                new ProtectionFromCardTypesEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CopyNextInstantOrSorceryCastThisTurnEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
