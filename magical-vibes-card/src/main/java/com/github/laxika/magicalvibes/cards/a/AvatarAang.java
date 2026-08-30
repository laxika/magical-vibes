package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.condition.AllBendingTypesCompletedThisTurn;

@CardRegistration(set = "TLA", collectorNumber = "207")
public class AvatarAang extends Card {

    public AvatarAang() {
        setBackFaceCard(new AangMasterOfElements());
        addEffect(EffectSlot.ON_CONTROLLER_BENDS, SequenceEffect.of(
                new DrawCardEffect(1),
                ConditionalEffect.unless(new AllBendingTypesCompletedThisTurn(),
                        new TransformToBackFaceEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "AangMasterOfElements";
    }
}
