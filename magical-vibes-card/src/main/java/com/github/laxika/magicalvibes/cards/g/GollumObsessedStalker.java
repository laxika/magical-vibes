package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect;

@CardRegistration(set = "LTC", collectorNumber = "26")
@CardRegistration(set = "LTC", collectorNumber = "109")
public class GollumObsessedStalker extends Card {

    public GollumObsessedStalker() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect(
                        "Gollum, Obsessed Stalker", new LifeGainedThisTurn(CountScope.CONTROLLER)));
    }
}
