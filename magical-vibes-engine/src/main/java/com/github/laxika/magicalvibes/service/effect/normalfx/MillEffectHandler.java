package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mill = (MillEffect) effect;

        // Source-relative amounts (e.g. CountersOnSource for Grindclock) use the live source
        // permanent when it is still on the battlefield, else the last-known snapshot.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, mill.count(),
                AmountContext.forStackEntry(entry, source)));

        switch (mill.recipient()) {
            case CONTROLLER -> graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
            case TARGET_PLAYER -> graveyardService.resolveMillPlayer(gameData, entry.getTargetId(), count);
            case EACH_OPPONENT -> {
                UUID controllerId = entry.getControllerId();
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (playerId.equals(controllerId)) continue;
                    graveyardService.resolveMillPlayer(gameData, playerId, count);
                }
            }
            case TARGET_SPELL_CONTROLLER -> {
                UUID spellControllerId = findTargetSpellControllerId(gameData, entry.getTargetId());
                if (spellControllerId != null) {
                    graveyardService.resolveMillPlayer(gameData, spellControllerId, count);
                }
            }
        }
    }

    /** Controller of the spell on the stack whose card id matches {@code targetCardId}, or null. */
    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) return null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                return se.getControllerId();
            }
        }
        return null;
    }
}
