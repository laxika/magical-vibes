package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "RNA", collectorNumber = "22")
@CardRegistration(set = "2X2", collectorNumber = "31")
@CardRegistration(set = "WOT", collectorNumber = "13")
@CardRegistration(set = "WOT", collectorNumber = "67")
@CardRegistration(set = "WOT", collectorNumber = "87")
public class SmotheringTithe extends Card {

    public SmotheringTithe() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2}?",
                MayPayPayer.TRIGGERING_PLAYER,
                CreateTokenEffect.ofTreasureToken(1),
                0
        ));
    }
}
