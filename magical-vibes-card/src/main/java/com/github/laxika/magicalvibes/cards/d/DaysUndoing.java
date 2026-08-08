package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;

@CardRegistration(set = "ORI", collectorNumber = "51")
public class DaysUndoing extends Card {

    public DaysUndoing() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesZonesIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));

        // If it's your turn, end the turn.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerTurn(), new EndTurnEffect()));
    }
}
