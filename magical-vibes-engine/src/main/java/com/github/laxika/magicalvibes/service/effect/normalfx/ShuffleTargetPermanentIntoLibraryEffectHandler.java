package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Shuffles the permanents the effect's subject names into their owners' libraries as one leave event, then shuffles
 * each of those libraries once, and records {@link StepKind#SHUFFLE_INTO_LIBRARY} (how many each owner shuffled in).
 * A specific object the subject names that has left the battlefield still has its owner's library shuffled
 * (CR 701.24c). When the next effect of the resolution shuffles libraries too, the libraries are left for it, so each
 * is shuffled once; that effect drains {@link StackEntry#getDeferredLibraryShuffleOwnerIds()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetPermanentIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPermanentIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var shuffle = (ShuffleTargetPermanentIntoLibraryEffect) effect;
        PermanentSubject subject = shuffle.subject();
        entry.recordStep(StepKind.SHUFFLE_INTO_LIBRARY, StepOutcome.NONE);
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, subject);

        Map<UUID, Integer> shuffledIn = new LinkedHashMap<>();
        Set<UUID> libraries = new LinkedHashSet<>();
        UUID thenControllerId = null;
        UUID thenOwnerId = null;
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : permanents) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                UUID ownerId = gameData.defaultControllerOf(permanent.getId());
                String name = FaceDownVisibility.publicName(gameData, permanent);
                if (!permanentRemovalService.removePermanentToLibraryUnshuffled(gameData, permanent)) {
                    continue;
                }
                shuffledIn.merge(ownerId, 1, Integer::sum);
                libraries.add(ownerId);
                if (thenOwnerId == null) {
                    thenControllerId = controllerId;
                    thenOwnerId = ownerId;
                }
                gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
                log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        Set<UUID> live = new HashSet<>(permanents.stream().map(Permanent::getId).toList());
        for (PermanentSubject named : specificObjects(subject)) {
            for (UUID departedId : departedCandidates(gameData, entry, effect, named)) {
                if (departedId == null || live.contains(departedId)) {
                    continue;
                }
                UUID ownerId = named instanceof PermanentSubject.Source ? ownerOfDepartedSource(entry)
                        : subjectResolutionService.player(gameData, entry, effect,
                                Subjects.ownerOf(Subjects.named(departedId)));
                if (ownerId != null && gameData.playerDecks.containsKey(ownerId)) {
                    libraries.add(ownerId);
                    if (thenOwnerId == null) {
                        thenOwnerId = ownerId;
                    }
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (shuffle.thenEffect() == null && nextEffectShufflesLibraries(entry)) {
            entry.getDeferredLibraryShuffleOwnerIds().addAll(libraries);
        } else {
            Set<UUID> owners = new LinkedHashSet<>(entry.getDeferredLibraryShuffleOwnerIds());
            entry.getDeferredLibraryShuffleOwnerIds().clear();
            owners.addAll(libraries);
            for (UUID ownerId : owners) {
                LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            }
        }
        if (!shuffledIn.isEmpty()) {
            entry.recordStep(StepKind.SHUFFLE_INTO_LIBRARY, StepOutcome.ofAmounts(shuffledIn));
        }

        if (shuffle.thenEffect() != null && thenOwnerId != null) {
            resolveThenEffect(gameData, entry, shuffle, thenControllerId, thenOwnerId);
        }
    }

    /**
     * The parts of the subject that name specific objects without targeting them ("this creature", "enchanted
     * creature", "the chosen creature"): one of them that left still has its owner's library shuffled (CR 701.24c). A
     * target that left is illegal and not affected (CR 608.2b); a set names no specific object.
     */
    private static List<PermanentSubject> specificObjects(PermanentSubject subject) {
        if (subject instanceof PermanentSubject.AllOf allOf) {
            return allOf.subjects().stream().filter(part -> !part.declaresTarget() && !part.isSet()).toList();
        }
        return subject.declaresTarget() || subject.isSet() ? List.of() : List.of(subject);
    }

    /**
     * The ids {@code named} names, those that left included; for "enchanted creature" of an Aura that left with it,
     * the host the activation captured as the entry's target.
     */
    private List<UUID> departedCandidates(GameData gameData, StackEntry entry, CardEffect effect,
                                          PermanentSubject named) {
        List<UUID> ids = subjectResolutionService.permanentIds(gameData, entry, effect, named).stream()
                .filter(Objects::nonNull).toList();
        if (ids.isEmpty() && named instanceof PermanentSubject.Attached && entry.getTargetId() != null
                && !gameData.playerIds.contains(entry.getTargetId())) {
            return List.of(entry.getTargetId());
        }
        return ids;
    }

    /** The owner of the entry's source that has left the battlefield, as the entry knows it. */
    private static UUID ownerOfDepartedSource(StackEntry entry) {
        if (entry.getOwnerId() != null) {
            return entry.getOwnerId();
        }
        return entry.getCard() != null && entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
    }

    /** Whether the effect after this one in the resolution shuffles libraries and drains the deferred owners. */
    private static boolean nextEffectShufflesLibraries(StackEntry entry) {
        int next = entry.getResolvingEffectIndex() + 1;
        List<CardEffect> effects = entry.getEffectsToResolve();
        if (entry.getResolvingEffectIndex() < 0 || next >= effects.size()) {
            return false;
        }
        CardEffect following = effects.get(next);
        return following instanceof ShuffleTargetPermanentIntoLibraryEffect
                || following instanceof EachPlayerShufflesZonesIntoLibraryEffect;
    }

    /**
     * Resolves the then-effect for the shuffled permanent's controller or owner. It is a separate instruction, so a
     * specific object that already left still has it resolved for its owner; a controller that isn't known skips it.
     */
    private void resolveThenEffect(GameData gameData, StackEntry entry, ShuffleTargetPermanentIntoLibraryEffect shuffle,
                                   UUID targetControllerId, UUID targetOwnerId) {
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
        StackEntry thenEntry = StackEntry.derivedFrom(entry, entry.getCard(), thenControllerId,
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
