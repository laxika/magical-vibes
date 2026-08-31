package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetPermanentIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPermanentIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var shuffle = (ShuffleTargetPermanentIntoLibraryEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());
        String name = target.getCard().getName();
        boolean removed = permanentRemovalService.removePermanentToLibraryShuffled(gameData, target);
        if (removed) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (removed && shuffle.thenEffect() != null) {
            UUID thenControllerId = switch (shuffle.recipient()) {
                case TARGET_CONTROLLER -> targetControllerId;
                case TARGET_OWNER -> targetOwnerId;
                case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
            };
            if (thenControllerId == null) {
                return;
            }

            UUID thenTargetId = switch (shuffle.recipient()) {
                case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
                case TARGET_OWNER_AS_TARGET -> targetOwnerId;
                default -> entry.getTargetId();
            };
            StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                    entry.getDescription(), List.of(shuffle.thenEffect()), thenTargetId,
                    entry.getSourcePermanentId());
            thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

            EffectHandler handler = effectHandlerRegistry.getHandler(shuffle.thenEffect());
            if (handler != null) {
                handler.resolve(gameData, thenEntry, shuffle.thenEffect());
            } else {
                log.warn("Game {} - No handler for then-effect: {}", gameData.id,
                        shuffle.thenEffect().getClass().getSimpleName());
            }
        }
    }
}
