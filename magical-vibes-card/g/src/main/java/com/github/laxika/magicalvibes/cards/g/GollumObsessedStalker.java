package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "26")
@CardRegistration(set = "LTC", collectorNumber = "109")
@CardRegistration(set = "LTC", collectorNumber = "428")
public class GollumObsessedStalker extends Card {

    public GollumObsessedStalker() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect(
                        "Gollum, Obsessed Stalker", new LifeGainedThisTurn(CountScope.CONTROLLER)));
    }
}
