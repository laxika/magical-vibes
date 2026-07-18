package com.github.laxika.magicalvibes.service.paradigm;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ParadigmCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ParadigmMayCastFromExileEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Paradigm (CR 702.192): exile on first resolve, then a delayed trigger at each precombat main
 * phase that creates a copy in exile the controller may cast without paying its mana cost.
 */
@Slf4j
@Service
public class ParadigmService {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final ParadigmCastSupport paradigmCastSupport;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    // @Lazy breaks cycles through InputCompletionService/TurnProgressionService and PlayerInputService.
    public ParadigmService(CopySupport copySupport,
                           ExileService exileService,
                           GameBroadcastService gameBroadcastService,
                           ParadigmCastSupport paradigmCastSupport,
                           @Lazy InputCompletionService inputCompletionService,
                           @Lazy PlayerInputService playerInputService) {
        this.copySupport = copySupport;
        this.exileService = exileService;
        this.gameBroadcastService = gameBroadcastService;
        this.paradigmCastSupport = paradigmCastSupport;
        this.inputCompletionService = inputCompletionService;
        this.playerInputService = playerInputService;
    }

    /**
     * Called after a spell with Paradigm resolves successfully. Exiles the spell and registers the
     * delayed trigger the first time this player resolves a spell with this name.
     */
    public void onParadigmSpellResolved(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        if (card == null || !card.getKeywords().contains(Keyword.PARADIGM)) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String spellName = card.getName();

        if (!entry.isCopy()) {
            Set<String> resolved = gameData.paradigmResolvedSpellNames
                    .computeIfAbsent(controllerId, id -> ConcurrentHashMap.newKeySet());
            if (!resolved.contains(spellName)) {
                resolved.add(spellName);
                Card prototype = copySupport.createCopyCard(card);
                gameData.paradigmDelayedTriggers.add(
                        new GameData.ParadigmDelayedTrigger(controllerId, prototype));
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " paradigm is registered."));
                log.info("Game {} - {} paradigm registered for {}", gameData.id, spellName, controllerId);
            }

            exileService.exileCard(gameData, controllerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " is exiled (paradigm)."));
        }
    }

    /**
     * Fires paradigm delayed triggers for the active player at the beginning of the precombat main phase.
     */
    public void firePrecombatMainTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        for (GameData.ParadigmDelayedTrigger trigger : gameData.paradigmDelayedTriggers) {
            if (!trigger.controllerId().equals(activePlayerId)) {
                continue;
            }
            Card prototype = trigger.spellPrototype();
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    prototype,
                    activePlayerId,
                    prototype.getName() + " paradigm",
                    new ArrayList<>(List.of(new ParadigmCastCopyEffect()))
            ));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(prototype, "'s paradigm triggers."));
            log.info("Game {} - {} paradigm trigger pushed onto stack", gameData.id, prototype.getName());
        }
    }

    /**
     * Creates a copy in exile and queues a may-cast prompt. Called when {@link ParadigmCastCopyEffect} resolves.
     */
    public void createCopyAndQueueMayCast(GameData gameData, StackEntry entry) {
        Card prototype = entry.getCard();
        UUID controllerId = entry.getControllerId();
        Card copy = copySupport.createCopyCard(prototype);
        exileService.exileCard(gameData, controllerId, copy);

        String description = "You may cast a copy of " + prototype.getName()
                + " without paying its mana cost.";
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                prototype,
                controllerId,
                List.of(new ParadigmMayCastFromExileEffect()),
                description,
                copy.getId()
        ));

        String logEntry = "A copy of " + prototype.getName() + " is created in exile.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("A copy of ", prototype, " is created in exile."));
        log.info("Game {} - Paradigm copy of {} created for {}", gameData.id, prototype.getName(), controllerId);

        if (!gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    public void handleMayCastChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID exileCardId = ability.targetCardId();
        if (exileCardId == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (!accepted) {
            gameData.removeFromExile(exileCardId);
            String logEntry = player.getUsername() + " declines to cast the paradigm copy.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines paradigm copy cast", gameData.id, player.getUsername());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        paradigmCastSupport.castFromExileWithoutPaying(gameData, player, exileCardId);
    }
}
