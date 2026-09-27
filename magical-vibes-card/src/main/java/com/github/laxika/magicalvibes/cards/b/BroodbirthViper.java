package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "C15", collectorNumber = "10")
public class BroodbirthViper extends Card {

    public BroodbirthViper() {
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
