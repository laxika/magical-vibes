package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Moves a target nonland permanent to a position in its owner's library and pauses for the owner
 * to choose whether it stays there or moves to the bottom.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetPermanentIntoLibraryNFromTopOrBottomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPermanentIntoLibraryNFromTopOrBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetPermanentIntoLibraryNFromTopOrBottomEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !permanentRemovalService.removePermanentToLibraryPosition(
                gameData, target, e.position())) {
            return;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
        UUID ownerId = findLibraryOwner(gameData, target);
        if (ownerId == null) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.TargetLibraryDestinationChoice(
                        ownerId,
                        target.getCard().getId(),
                        target.getCard().getName(),
                        positionLabel(e.position())));
        log.info("Game {} - awaiting {} to choose the library destination for {}",
                gameData.id, gameData.playerIdToName.get(ownerId), target.getCard().getName());
    }

    private UUID findLibraryOwner(GameData gameData, Permanent target) {
        UUID cardId = target.getCard().getId();
        return gameData.playerDecks.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(card -> card.getId().equals(cardId)))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static String positionLabel(int position) {
        return switch (position) {
            case 0 -> "Top";
            case 1 -> "Second from the top";
            case 2 -> "Third from the top";
            default -> (position + 1) + "th from top";
        };
    }
}
