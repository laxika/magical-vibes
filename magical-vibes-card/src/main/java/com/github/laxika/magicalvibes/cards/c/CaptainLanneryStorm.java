package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificedPermanentSubtypeConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "136")
public class CaptainLanneryStorm extends Card {

    public CaptainLanneryStorm() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new SacrificedPermanentSubtypeConditionalEffect(
                        CardSubtype.TREASURE,
                        new BoostSelfEffect(1, 0)
                ));
    }
}
