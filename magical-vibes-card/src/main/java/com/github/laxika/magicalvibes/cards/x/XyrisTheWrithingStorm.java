package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForDamagedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExceptFirstDrawStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "175")
public class XyrisTheWrithingStorm extends Card {

    public XyrisTheWrithingStorm() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new ExceptFirstDrawStepTriggerEffect(new CreateTokenEffect(
                        "Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of())));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DrawCardEffect(new EventValue()),
                new DrawCardForDamagedPlayerEffect(new EventValue())));
    }
}
