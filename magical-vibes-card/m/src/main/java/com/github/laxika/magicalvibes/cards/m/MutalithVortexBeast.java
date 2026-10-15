package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachOpponentEffect;

@CardRegistration(set = "40K", collectorNumber = "134")
public class MutalithVortexBeast extends Card {

    public MutalithVortexBeast() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new FlipCoinForEachOpponentEffect(
                new DrawCardEffect(),
                new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER)));
    }
}
