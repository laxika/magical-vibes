package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.FirstEquipFreeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "AFR", collectorNumber = "219")
public class BruenorBattlehammer extends Card {

    public BruenorBattlehammer() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new Scaled(new AttachmentsOnSource(false, true), 2),
                new Fixed(0),
                GrantScope.ALL_OWN_CREATURES,
                null,
                true));
        addEffect(EffectSlot.STATIC, new FirstEquipFreeEffect());
    }
}
