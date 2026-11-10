package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "244")
public class SwordOfForgeAndFrontier extends Card {

    public SwordOfForgeAndFrontier() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED, CardColor.GREEN), GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(2, false),
                new PlayAdditionalLandsEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
