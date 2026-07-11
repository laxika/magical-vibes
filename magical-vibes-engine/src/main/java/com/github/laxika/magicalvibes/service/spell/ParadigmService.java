package com.github.laxika.magicalvibes.service.spell;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.MayCastFromExileWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Paradigm keyword (Secrets of Strixhaven): on first resolution, exile the spell and register a delayed
 * trigger that creates a free copy in exile at the beginning of each of the controller's precombat main phases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParadigmService {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    /**
     * Registers the paradigm delayed trigger when a non-copy spell with {@link Keyword#PARADIGM} resolves
     * for the first time this game for that controller and spell name.
     */
    public void handleParadigmSpellResolved(GameData gameData, StackEntry entry) {
        if (entry.isCopy()) {
            return;
        }
        Card card = entry.getCard();
        if (card == null || !card.getKeywords().contains(Keyword.PARADIGM)) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String spellName = card.getName();
        Set<String> resolved = gameData.paradigmResolvedSpellNames.computeIfAbsent(
                controllerId, ignored -> ConcurrentHashMap.newKeySet());
        if (!resolved.add(spellName)) {
            return;
        }

        Card template = copySupport.createCopyCard(card);
        gameData.paradigmDelayedTriggers.add(new GameData.ParadigmDelayedTrigger(
                controllerId, template, gameData.turnNumber));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = spellName + " creates a Paradigm delayed trigger for " + playerName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} registers paradigm delayed trigger for {}", gameData.id, spellName, playerName);
    }

    /**
     * Fires paradigm delayed triggers for the active player at the beginning of the precombat main phase.
     *
     * @return {@code true} if at least one may-cast prompt was queued
     */
    public boolean fireParadigmTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        int currentTurn = gameData.turnNumber;
        boolean queued = false;

        for (GameData.ParadigmDelayedTrigger trigger : gameData.paradigmDelayedTriggers) {
            if (!trigger.controllerId().equals(activePlayerId) || currentTurn <= trigger.registeredOnTurn()) {
                continue;
            }

            Card copy = copySupport.createCopyCard(trigger.spellTemplate());
            exileService.exileCard(gameData, trigger.controllerId(), copy);

            gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                    copy,
                    trigger.controllerId(),
                    List.of(new MayCastFromExileWithoutPayingManaCostEffect()),
                    trigger.spellTemplate().getName() + " — Cast a copy without paying its mana cost?"
            ));
            queued = true;

            String playerName = gameData.playerIdToName.get(trigger.controllerId());
            String logEntry = trigger.spellTemplate().getName()
                    + "'s Paradigm trigger creates a copy in exile for " + playerName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Paradigm trigger creates {} copy for {}",
                    gameData.id, trigger.spellTemplate().getName(), playerName);
        }

        return queued;
    }
}
