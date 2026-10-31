package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "100")
@CardRegistration(set = "VOC", collectorNumber = "131")
@CardRegistration(set = "C17", collectorNumber = "116")
public class MalakirBloodwitch extends Card {

    public MalakirBloodwitch() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER),
                LoseLifeRecipient.EACH_OPPONENT,
                true));
    }
}
