package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;

@CardRegistration(set = "C17", collectorNumber = "17")
public class KheruMindEater extends Card {

    public KheruMindEater() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                TargetPlayerExilesFromHandEffect.faceDown(1));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
    }
}
