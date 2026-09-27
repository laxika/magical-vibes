package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

@CardRegistration(set = "SLD", collectorNumber = "1570")
@CardRegistration(set = "SLD", collectorNumber = "1864")
@CardRegistration(set = "SLD", collectorNumber = "1884")
@CardRegistration(set = "C15", collectorNumber = "51")
public class BladeOfSelves extends Card {

    public BladeOfSelves() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad(),
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
