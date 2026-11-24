package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "26")
public class SwordOfWealthAndPower extends Card {

    public SwordOfWealthAndPower() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantEffectEffect(
                new ProtectionFromCardTypesEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                Subjects.attachedCreature()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1),
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect())));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
