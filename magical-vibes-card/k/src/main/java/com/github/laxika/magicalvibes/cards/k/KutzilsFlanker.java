package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesLeftBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "20")
@CardRegistration(set = "LCI", collectorNumber = "355")
public class KutzilsFlanker extends Card {

    public KutzilsFlanker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on this creature for each creature that left the battlefield under your control this turn",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                                new CreaturesLeftBattlefieldThisTurn(CountScope.CONTROLLER))),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 2 life and scry 2",
                        List.of(new GainLifeEffect(2), new ScryEffect(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target player's graveyard",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE))
        )));
    }
}
