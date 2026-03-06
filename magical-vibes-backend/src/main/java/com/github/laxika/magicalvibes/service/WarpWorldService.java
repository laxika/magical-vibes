package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarpWorldService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final CreatureControlService creatureControlService;
    private final CardViewFactory cardViewFactory;
    private final SessionManager sessionManager;

    public void beginNextPendingLibraryBottomReorder(GameData gameData) {
        LibraryBottomReorderRequest request = gameData.pendingLibraryBottomReorders.pollFirst();
        if (request == null) {
            return;
        }

        UUID playerId = request.playerId();
        List<Card> cards = request.cards();
        if (cards == null || cards.size() <= 1) {
            if (cards != null && cards.size() == 1) {
                gameData.playerDecks.get(playerId).add(cards.getFirst());
            }
            beginNextPendingLibraryBottomReorder(gameData);
            return;
        }

        gameData.interaction.beginLibraryReorder(playerId, cards, true);

        List<CardView> cardViews = cards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
        ));

        String logMsg = gameData.playerIdToName.get(playerId) + " orders cards for the bottom of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
    }

    public void beginNextPendingWarpWorldAuraChoice(GameData gameData) {
        WarpWorldAuraChoiceRequest request = gameData.warpWorldOperation.pendingAuraChoices.pollFirst();
        if (request == null) {
            return;
        }

        gameData.interaction.setPendingAuraCard(request.auraCard());
        playerInputService.beginPermanentChoice(
                gameData,
                request.controllerId(),
                request.validTargetIds(),
                "Choose a permanent for " + request.auraCard().getName() + " to enchant."
        );
    }

    public void placePendingWarpWorldEnchantments(GameData gameData) {
        Set<CardType> enterTappedTypes = gameData.warpWorldOperation.enterTappedTypesSnapshot;
        for (WarpWorldEnchantmentPlacement placement : gameData.warpWorldOperation.pendingEnchantmentPlacements) {
            UUID controllerId = placement.controllerId();
            Card card = placement.card();
            Permanent permanent = new Permanent(card);
            if (placement.attachmentTargetId() != null) {
                permanent.setAttachedTo(placement.attachmentTargetId());
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

            if (placement.attachmentTargetId() != null) {
                boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    Permanent target = gameQueryService.findPermanentById(gameData, placement.attachmentTargetId());
                    if (target != null) {
                        creatureControlService.stealPermanent(gameData, controllerId, target);
                    }
                }
            }
        }
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
    }

    public void finalizePendingWarpWorld(GameData gameData) {
        if (gameData.warpWorldOperation.sourceName == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> creatures = gameData.warpWorldOperation.pendingCreaturesByPlayer.getOrDefault(playerId, List.of());
            for (Card card : creatures) {
                battlefieldEntryService.processCreatureETBEffects(gameData, playerId, card, null, false);
            }
        }

        if (!gameData.interaction.isAwaitingInput() && gameData.warpWorldOperation.needsLegendChecks) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                legendRuleService.checkLegendRule(gameData, playerId);
            }
        }

        gameBroadcastService.logAndBroadcast(gameData,
                gameData.warpWorldOperation.sourceName + " shuffles all permanents into libraries and warps the world.");

        gameData.warpWorldOperation.pendingCreaturesByPlayer.clear();
        gameData.warpWorldOperation.pendingAuraChoices.clear();
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.clear();
        gameData.warpWorldOperation.needsLegendChecks = false;
        gameData.warpWorldOperation.sourceName = null;
    }
}
