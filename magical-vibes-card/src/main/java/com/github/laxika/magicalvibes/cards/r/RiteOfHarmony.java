package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

@CardRegistration(set = "MID", collectorNumber = "236")
public class RiteOfHarmony extends Card {

    public RiteOfHarmony() {
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new DrawCardEffect()));
        addCastingOption(new FlashbackCast("{2}{G}{W}"));
    }
}
