package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "84")
@CardRegistration(set = "FIC", collectorNumber = "174")
public class KefkaDancingMad extends Card {

    public KefkaDancingMad() {
        // During your turn, Kefka has indestructible.
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // At the beginning of your end step, exile a card at random from each opponent's graveyard.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect());
    }
}
