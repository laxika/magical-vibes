package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOutsideCommanderColorIdentityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "SLD", collectorNumber = "1339")
@CardRegistration(set = "SLD", collectorNumber = "1733")
public class CommandersPlate extends Card {

    public CommandersPlate() {
        addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromColorsOutsideCommanderColorIdentityEffect());
        addActivatedAbility(new EquipActivatedAbility(
                "{3}", new PermanentIsCommanderPredicate(), "Target must be a commander"));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
