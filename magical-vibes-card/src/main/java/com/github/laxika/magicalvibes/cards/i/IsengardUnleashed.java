package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect;

@CardRegistration(set = "LTC", collectorNumber = "495")
@CardRegistration(set = "LTC", collectorNumber = "539")
public class IsengardUnleashed extends Card {

    public IsengardUnleashed() {
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());
        addEffect(EffectSlot.SPELL,
                new MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect(3));
        addCastingOption(new FlashbackCast("{4}{R}{R}{R}"));
    }
}
