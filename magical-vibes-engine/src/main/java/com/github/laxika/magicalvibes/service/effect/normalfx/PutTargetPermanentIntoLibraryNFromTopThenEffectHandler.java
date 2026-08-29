package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopThenEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves a library-tuck effect followed by an existing effect, preserving the removed permanent's
 * controller or owner for the follow-up.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetPermanentIntoLibraryNFromTopThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PutTargetPermanentIntoLibraryNFromTopEffectHandler putIntoLibraryHandler;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPermanentIntoLibraryNFromTopThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetPermanentIntoLibraryNFromTopThenEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());

        putIntoLibraryHandler.resolve(gameData, entry,
                new PutTargetPermanentIntoLibraryNFromTopEffect(e.position()));

        UUID thenControllerId = switch (e.recipient()) {
            case TARGET_CONTROLLER -> targetControllerId;
            case TARGET_OWNER -> targetOwnerId;
            case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
        };
        if (thenControllerId == null) {
            return;
        }

        UUID thenTargetId = switch (e.recipient()) {
            case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
            case TARGET_OWNER_AS_TARGET -> targetOwnerId;
            default -> entry.getTargetId();
        };
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                entry.getDescription(), List.of(e.thenEffect()), thenTargetId, entry.getSourcePermanentId());
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        EffectHandler handler = effectHandlerRegistry.getHandler(e.thenEffect());
        if (handler != null) {
            handler.resolve(gameData, thenEntry, e.thenEffect());
        } else {
            log.warn("Game {} - No handler for then-effect: {}", gameData.id,
                    e.thenEffect().getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
