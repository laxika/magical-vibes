package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;

@CardRegistration(set = "HOB", collectorNumber = "47")
public class MastersCouncillors extends Card {

    public MastersCouncillors() {
        GraveyardsAtLeast sevenCardGraveyards = new GraveyardsAtLeast(7);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new Scaled(sevenCardGraveyards, 2), new Fixed(0), GrantScope.SELF));

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new NthCardDrawTriggerEffect(2, new MillEffect(3, MillRecipient.TARGET_PLAYER)));
    }
}
