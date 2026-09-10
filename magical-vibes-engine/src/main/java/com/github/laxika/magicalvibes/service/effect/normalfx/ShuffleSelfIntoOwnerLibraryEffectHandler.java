package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleSelfIntoOwnerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSelfIntoOwnerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, self.getId());
        UUID sourceOwnerId = gameData.defaultControllerOf(self.getId());
        String name = self.getCard().getName();
        boolean removed = permanentRemovalService.removePermanentToLibraryShuffled(gameData, self);
        if (removed) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        ShuffleSelfIntoOwnerLibraryEffect shuffle = (ShuffleSelfIntoOwnerLibraryEffect) effect;
        if (!removed || shuffle.thenEffect() == null) {
            return;
        }

        UUID thenControllerId = switch (shuffle.recipient()) {
            case TARGET_CONTROLLER -> sourceControllerId;
            case TARGET_OWNER -> sourceOwnerId;
            case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
        };
        if (thenControllerId == null) {
            return;
        }

        UUID thenTargetId = switch (shuffle.recipient()) {
            case TARGET_CONTROLLER_AS_TARGET -> sourceControllerId;
            case TARGET_OWNER_AS_TARGET -> sourceOwnerId;
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
