package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "SNC", collectorNumber = "235")
public class CementShoes extends Card {

    public CementShoes() {
        addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(DoesntUntapEffect.enchanted());
        addStaticEffect(new GrantTriggeredAbilityEffect(
                EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new TapPermanentsEffect(TapUntapScope.SELF),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
