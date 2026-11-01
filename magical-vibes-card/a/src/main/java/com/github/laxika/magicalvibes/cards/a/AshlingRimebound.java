package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class AshlingRimebound extends Card {

    public AshlingRimebound() {
        AwardAnyColorManaEffect mana = new AwardAnyColorManaEffect(
                2, ManaSpendRestriction.MANA_VALUE_AT_LEAST_FOUR);
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), mana);
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, mana, TriggerMode.INDEPENDENT);

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{R}", new TransformSelfEffect(),
                        "Pay {R} to transform Ashling?"), TriggerMode.INDEPENDENT);
    }
}
