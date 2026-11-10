package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreCardsInHandThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "114")
public class SokenzanRenegade extends Card {

    public SokenzanRenegade() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(1));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(1));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreCardsInHandThanEachOtherPlayer()),
                new PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect());
    }
}
