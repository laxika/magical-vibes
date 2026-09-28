package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CouncilDilemmaEffect;

@CardRegistration(set = "NCC", collectorNumber = "10")
@CardRegistration(set = "NCC", collectorNumber = "109")
public class TivitSellerOfSecrets extends Card {

    public TivitSellerOfSecrets() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CouncilDilemmaEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CouncilDilemmaEffect());
    }
}
