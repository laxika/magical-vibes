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
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        Permanent self = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        ShuffleSelfIntoOwnerLibraryEffect shuffle = (ShuffleSelfIntoOwnerLibraryEffect) effect;
        if (self == null) {
            // CR 701.24c: the owner's library is shuffled even if the object is no longer where it was.
            UUID ownerId = ownerOfLeftSource(entry);
            shuffleDeferredLibraries(gameData, entry, ownerId);
            resolveThenEffect(gameData, entry, shuffle, null, ownerId);
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, self.getId());
        UUID sourceOwnerId = gameData.defaultControllerOf(self.getId());
        String name = self.getCard().getName();
        boolean removed = permanentRemovalService.removePermanentToLibraryUnshuffled(gameData, self);
        if (removed) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }
        shuffleDeferredLibraries(gameData, entry, sourceOwnerId);

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (!removed) {
            return;
        }
        resolveThenEffect(gameData, entry, shuffle, sourceControllerId, sourceOwnerId);
    }

    /**
     * Resolves the then-effect, if any. It is a separate instruction, so it still resolves when the object
     * has already left the battlefield; recipients tied to the object's controller are skipped when unknown.
     */
    private void resolveThenEffect(GameData gameData, StackEntry entry, ShuffleSelfIntoOwnerLibraryEffect shuffle,
                                   UUID sourceControllerId, UUID sourceOwnerId) {
        if (shuffle.thenEffect() == null) {
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

    private UUID ownerOfLeftSource(StackEntry entry) {
        if (entry.getOwnerId() != null) {
            return entry.getOwnerId();
        }
        return entry.getCard() != null && entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
    }

    private void shuffleDeferredLibraries(GameData gameData, StackEntry entry, UUID sourceOwnerId) {
        Set<UUID> owners = new LinkedHashSet<>(entry.getDeferredLibraryShuffleOwnerIds());
        entry.getDeferredLibraryShuffleOwnerIds().clear();
        if (sourceOwnerId != null) {
            owners.add(sourceOwnerId);
        }
        for (UUID ownerId : owners) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }
    }
}
