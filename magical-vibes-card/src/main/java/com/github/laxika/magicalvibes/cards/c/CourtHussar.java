package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "DIS", collectorNumber = "22")
public class CourtHussar extends Card {

    public CourtHussar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.WHITE)), new SacrificeSelfEffect()));
    }
}
