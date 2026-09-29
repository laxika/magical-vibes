package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class MahadiEmporiumMaster extends Card {

    public MahadiEmporiumMaster() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.ofTreasureToken(new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
