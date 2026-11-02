package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "69")
public class SanctuaryBlade extends Card {

    public SanctuaryBlade() {
        addTrigger(Triggers.becomesAttachedToCreature(), new ChooseColorForSourceEffect());
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromChosenColorEffect(GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
