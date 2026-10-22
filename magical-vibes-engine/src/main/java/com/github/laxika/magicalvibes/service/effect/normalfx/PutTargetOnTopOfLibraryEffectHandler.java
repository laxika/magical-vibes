package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetOnTopOfLibraryEffect) effect;
        boolean bothAndShuffle = e.scope() == PutOnTopOfLibraryScope.SELF_AND_TARGET;

        if (e.scope() == PutOnTopOfLibraryScope.TARGET) {
            List<PutTargetOnTopOfLibraryEffect> siblings = siblingTargetEffects(entry);
            if (siblings.size() > 1 && indexOfIdentity(siblings, e) >= 0) {
                tuckTogether(gameData, entry, siblings);
                return;
            }
        }

        Set<UUID> ownersToShuffle = new LinkedHashSet<>();
        if (e.scope() != PutOnTopOfLibraryScope.SELF) {
            UUID targetId = entry.targetsForEffect(e).stream().findFirst().orElse(entry.getTargetId());
            tuckRecordingOwner(gameData, gameQueryService.findPermanentById(gameData, targetId),
                    bothAndShuffle, ownersToShuffle);
        }
        if (e.scope() != PutOnTopOfLibraryScope.TARGET) {
            tuckRecordingOwner(gameData, gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()),
                    bothAndShuffle, ownersToShuffle);
        }
        for (UUID ownerId : ownersToShuffle) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Puts the permanent on top of its owner's library. When {@code deferShuffle} is set, the owner is
     * recorded instead of shuffled, so that both halves of "then those players shuffle" shuffle each
     * owner's library once after both cards have moved.
     */
    private void tuckRecordingOwner(GameData gameData, Permanent permanent, boolean deferShuffle,
                                    Set<UUID> ownersToShuffle) {
        if (permanent == null) return;
        UUID ownerId = gameData.stolenCreatures.get(permanent.getId());
        if (ownerId == null) {
            ownerId = gameData.ownerOf(permanent.getOriginalCard());
        }
        if (ownerId == null) {
            ownerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        }
        if (permanentRemovalService.removePermanentToLibraryTop(gameData, permanent, false)) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library", gameData.id, permanent.getCard().getName());
            if (deferShuffle && ownerId != null) {
                ownersToShuffle.add(ownerId);
            }
        }
    }

    private void tuck(GameData gameData, Permanent permanent, boolean shuffle) {
        if (permanent == null) return;

        if (permanentRemovalService.removePermanentToLibraryTop(gameData, permanent, shuffle)) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library", gameData.id, permanent.getCard().getName());
        }
    }

    private static List<PutTargetOnTopOfLibraryEffect> siblingTargetEffects(StackEntry entry) {
        Card targeting = entry.getTargetingCard();
        if (targeting == null) {
            return List.of();
        }
        return targeting.getEffects(EffectSlot.SPELL).stream()
                .filter(PutTargetOnTopOfLibraryEffect.class::isInstance)
                .map(PutTargetOnTopOfLibraryEffect.class::cast)
                .filter(sibling -> sibling.scope() == PutOnTopOfLibraryScope.TARGET)
                .toList();
    }

    private static int indexOfIdentity(List<PutTargetOnTopOfLibraryEffect> effects, CardEffect effect) {
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Puts the targets of every sibling effect on top of their owners' libraries at once (Plow Under:
     * "Put two target lands on top of their owners' libraries"). Whichever sibling resolves first
     * handles them all, so later siblings find nothing left to move. Cards that land on the same
     * library together are ordered by that library's owner (CR 401.4).
     */
    private void tuckTogether(GameData gameData, StackEntry entry, List<PutTargetOnTopOfLibraryEffect> siblings) {
        List<UUID> targetIds = new ArrayList<>();
        for (PutTargetOnTopOfLibraryEffect sibling : siblings) {
            // Each sibling owns its own target group; the group being resolved right now must not
            // stand in for the others, or the first effect only ever finds its own target.
            int group = entry.getTargetingCard() == null ? -1
                    : entry.getTargetingCard().getEffectTargetIndex(sibling);
            List<UUID> bound = group >= 0 ? entry.targetsForGroup(group) : null;
            List<UUID> siblingTargets = bound != null ? bound : entry.targetsForEffect(sibling);
            for (UUID targetId : siblingTargets) {
                if (!targetIds.contains(targetId)) {
                    targetIds.add(targetId);
                }
            }
        }

        Map<UUID, Integer> deckSizesBefore = new HashMap<>();
        gameData.playerDecks.forEach((ownerId, deck) -> deckSizesBefore.put(ownerId, deck.size()));

        for (UUID targetId : targetIds) {
            tuck(gameData, gameQueryService.findPermanentById(gameData, targetId), false);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameData.playerDecks.forEach((ownerId, deck) -> {
            int added = deck.size() - deckSizesBefore.getOrDefault(ownerId, deck.size());
            if (added < 2) {
                return;
            }
            PendingInteraction.LibraryReorder choice = new PendingInteraction.LibraryReorder(
                    ownerId, List.copyOf(deck.subList(0, added)), false, ownerId,
                    "Choose the order of the cards put on top of your library.",
                    0, null, false, false, Zone.LIBRARY, null);
            if (!gameData.interaction.isAwaitingInput()) {
                interactionHandlerRegistry.begin(gameData, choice);
            } else {
                gameData.pendingInteractions.addLast(choice);
            }
        });
    }
}
