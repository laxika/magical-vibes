package com.github.laxika.magicalvibes.service.paradigm;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ParadigmCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ParadigmMayCastFromExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
    private final GameLogService gameLogService;
    private final ParadigmCastSupport paradigmCastSupport;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    // @Lazy breaks cycles through InputCompletionService/TurnProgressionService and PlayerInputService.
    public ParadigmService(CopySupport copySupport,
                           ExileService exileService,
                           GameLogService gameLogService,
                           ParadigmCastSupport paradigmCastSupport,
                           @Lazy InputCompletionService inputCompletionService,
                           @Lazy PlayerInputService playerInputService) {
        this.copySupport = copySupport;
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.paradigmCastSupport = paradigmCastSupport;
        this.inputCompletionService = inputCompletionService;
        this.playerInputService = playerInputService;
    }

    /**
     * Called after a spell with Paradigm resolves successfully. Registers the delayed trigger the first
     * time this player resolves a spell with this name (copies included), and exiles the spell when it
     * is the original card rather than a copy.
     */
    public void onParadigmSpellResolved(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        if (card == null || !card.getKeywords().contains(Keyword.PARADIGM)) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String spellName = card.getName();

        Set<String> resolved = gameData.paradigmResolvedSpellNames
                .computeIfAbsent(controllerId, id -> ConcurrentHashMap.newKeySet());
        if (!resolved.contains(spellName)) {
            resolved.add(spellName);
            // Paradigm's delayed triggered ability, made by the spell as it resolved, its source a copy of it
            // (CR 702.192a, 603.7d): "at the beginning of each of your precombat main phases for the rest of the game".
            Card prototype = copySupport.createCopyCard(card);
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(controllerId,
                    prototype, new TriggeredAbility(Triggers.precombatMainPhase(ActivePlayer.YOU),
                            new ParadigmCastCopyEffect()), null, null, DelayedReferents.NONE, null));
            gameLogService.append(gameData, GameLog.cardThen(card, " paradigm is registered."));
            log.info("Game {} - {} paradigm registered for {}", gameData.id, spellName, controllerId);
        }

        if (!entry.isCopy()) {
            exileService.exileCard(gameData, controllerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled (paradigm)."));
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

        
        gameLogService.append(gameData, GameLog.textCardText("A copy of ", prototype, " is created in exile."));
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines paradigm copy cast", gameData.id, player.getUsername());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        paradigmCastSupport.castFromExileWithoutPaying(gameData, player, exileCardId);
    }
}
