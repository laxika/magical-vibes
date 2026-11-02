package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CLB", collectorNumber = "282")
public class MahadiEmporiumMaster extends Card {

    public MahadiEmporiumMaster() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                CreateTokenEffect.ofTreasureToken(new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
