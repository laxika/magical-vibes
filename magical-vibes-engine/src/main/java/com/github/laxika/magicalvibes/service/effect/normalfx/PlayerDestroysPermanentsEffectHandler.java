package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDestroysPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PlayerDestroysPermanentsEffect}: the {@code recipient} routes who destroys their
 * own permanents (controller / target player), and {@link DestructionSupport#destroyPlayerMatchingPermanents}
 * either destroys all matching (≤ count) or prompts the player to choose which to destroy. Destruction
 * respects regeneration/indestructible; when a choice is needed resolution pauses and resumes via the
 * {@code MultiPermanentChoiceContext.ForcedDestroy} completion. Used by Burning of Xinye.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerDestroysPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerDestroysPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayerDestroysPermanentsEffect) effect;

        UUID playerId = switch (e.recipient()) {
            case CONTROLLER -> entry.getControllerId();
            case TARGET_PLAYER -> entry.getTargetId();
        };
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        int count = evaluateCount(gameData, entry, e);
        destructionSupport.destroyPlayerMatchingPermanents(gameData, playerId, count, e.filter(),
                entry.getCard().getName());
    }

    private int evaluateCount(GameData gameData, StackEntry entry, PlayerDestroysPermanentsEffect e) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return amountEvaluationService.evaluate(gameData, e.count(), AmountContext.forStackEntry(entry, source));
    }
}
