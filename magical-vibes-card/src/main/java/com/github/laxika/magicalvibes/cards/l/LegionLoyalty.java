package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

@CardRegistration(set = "TDC", collectorNumber = "122")
@CardRegistration(set = "M3C", collectorNumber = "171")
public class LegionLoyalty extends Card {

    public LegionLoyalty() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad(),
                GrantScope.ALL_OWN_CREATURES));
    }
}
