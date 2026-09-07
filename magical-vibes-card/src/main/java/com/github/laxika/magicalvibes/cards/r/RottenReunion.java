package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "MID", collectorNumber = "119")
public class RottenReunion extends Card {

    public RottenReunion() {
        addEffect(EffectSlot.SPELL, new ExileCardsFromGraveyardEffect(1, 0));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombieWithDecayed(1));
        addCastingOption(new FlashbackCast("{1}{B}"));
    }
}
