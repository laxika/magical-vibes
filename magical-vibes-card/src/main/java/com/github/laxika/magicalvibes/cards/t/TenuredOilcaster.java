package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MOM", collectorNumber = "126")
public class TenuredOilcaster extends Card {

    public TenuredOilcaster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_BLOCK, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_BLOCK, new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
