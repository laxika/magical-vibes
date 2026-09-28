package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredAbilitiesCantCauseSacrificeOrExileCreatureTokensEffect;

@CardRegistration(set = "WHO", collectorNumber = "146")
public class TheMasterMultiplied extends Card {

    public TheMasterMultiplied() {
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleForControlledPermanentsEffect());
        addEffect(EffectSlot.STATIC, new TriggeredAbilitiesCantCauseSacrificeOrExileCreatureTokensEffect());
    }
}
