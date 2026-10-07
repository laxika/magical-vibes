package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves an event-bound "put the same counters on this source" effect. */
@Component
@RequiredArgsConstructor
public class PutSameCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSameCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutSameCountersOnSourceEffect e = (PutSameCountersOnSourceEffect) effect;
        if (e.counterType() == null || e.amount() <= 0 || e.targetPermanentId() == null
                || entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, e.targetPermanentId());
        // The number and kind of counters were fixed by the triggering event, so a creature that has
        // since left the battlefield (e.g. died to its -1/-1 counter) still lets them be copied.
        if (source == null || (e.placingPlayerId() == null && target != null
                && (source.getId().equals(target.getId())
                || !gameQueryService.isCreature(gameData, target)
                || (e.requiresNonKree()
                && gameQueryService.effectiveCreatureSubtypes(gameData, target).contains(CardSubtype.KREE))))) {
            return;
        }

        UUID placingPlayerId = e.placingPlayerId() != null
                ? e.placingPlayerId() : entry.getControllerId();
        permanentCounterSupport.placeCounterOnPermanentForPlayer(
                gameData, entry, source, e.counterType(), e.amount(), placingPlayerId);
    }
}
