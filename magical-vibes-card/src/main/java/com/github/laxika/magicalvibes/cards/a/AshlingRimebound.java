package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

public class AshlingRimebound extends Card {

    public AshlingRimebound() {
        AwardAnyColorManaEffect mana = new AwardAnyColorManaEffect(
                2, ManaSpendRestriction.MANA_VALUE_AT_LEAST_FOUR);
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, mana);
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, mana);

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{R}", new TransformSelfEffect(),
                        "Pay {R} to transform Ashling?"));
    }
}
