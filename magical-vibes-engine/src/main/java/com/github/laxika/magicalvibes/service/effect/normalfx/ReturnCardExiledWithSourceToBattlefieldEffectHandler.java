package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Returns one card exiled "with" the source permanent to the battlefield under the ability
 * controller's control (CR 110.2a). A single exiled card returns straight away; with several the
 * controller chooses which one via a {@code LIBRARY_REVEAL_CHOICE} routed by
 * {@link PendingReturnExiledWithSourceCard}. Used by Purgatory's upkeep trigger.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnCardExiledWithSourceToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameMutationCoordinator mutationCoordinator;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardExiledWithSourceToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<Card> matching = gameData.exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .map(ExiledCardEntry::card)
                .toList();

        if (matching.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " has no cards exiled with " + sourceName + "."));
            return;
        }

        if (matching.size() == 1) {
            returnToBattlefield(gameData, controllerId, matching.getFirst(), sourceName);
            return;
        }

        gameData.queueInteraction(new PendingReturnExiledWithSourceCard(true, controllerId));
        List<UUID> validIds = matching.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(matching), validIds,
                false, true, false, false, false, 0, null, 1,
                "Choose a card exiled with " + sourceName + " to return to the battlefield."));
        mutationCoordinator.invalidateAllPlayerViews(gameData);

        log.info("Game {} - {} chooses from {} cards exiled with {}",
                gameData.id, controllerName, matching.size(), sourceName);
    }

    /** Shared with the multi-card choice path in {@code LibraryChoiceHandlerService}. */
    public void returnToBattlefield(GameData gameData, UUID controllerId, Card card, String sourceName) {
        if (!gameData.removeFromExile(card.getId())) {
            return;
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(card));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " returns ", card,
                " from exile to the battlefield."));
        log.info("Game {} - {} returns from exile ({}) to the battlefield",
                gameData.id, card.getName(), sourceName);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
    }
}
