package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Map;

/**
 * Static: "Once during each of your turns, you may play a land from your graveyard or cast a
 * permanent matching {@link #filter()} from your graveyard." The used-per-turn marker is shared
 * between the land-play and spell-cast alternatives.
 */
public record PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(CardPredicate filter)
        implements CastSpellsFromGraveyardPermission {

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }

    @Override
    public boolean permitsLandPlayFromGraveyard() {
        return true;
    }

    @Override
    public Map<EffectSlot, List<CardEffect>> grantedTriggeredEffectsOnEntry() {
        return Map.of(EffectSlot.ON_DEATH, List.of(SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(), new GainLifeEffect(2))));
    }
}
