package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Casts a real (non-copy) card that already sits in exile "without paying its mana cost", choosing a
 * target first when required. Used when a player is offered to play a card put into exile by an
 * effect such as Guile's counter-replacement. Timing/priority restrictions are ignored (the play is
 * part of another effect's resolution); if the spell can't be legally cast it stays exiled.
 */
@Slf4j
@Component
public class ExileFreeCastSupport {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;

    // @Lazy mirrors ParadigmCastSupport: breaks cycles through InputCompletionService/PlayerInputService.
    public ExileFreeCastSupport(GameBroadcastService gameBroadcastService,
                                @Lazy PlayerInputService playerInputService,
                                @Lazy TriggerCollectionService triggerCollectionService,
                                @Lazy InputCompletionService inputCompletionService,
                                ExileCastTargetSupport exileCastTargetSupport) {
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.inputCompletionService = inputCompletionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
    }

    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId) {
        UUID playerId = player.getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card card = exiledEntry.card();
        String playerName = player.getUsername();
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(card)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
            boolean multiTarget = card.getMaxTargets() > 1;
            boolean hasLegalTargets = multiTarget
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, playerId)
                    : !firstCandidates.isEmpty();

            if (!hasLegalTargets) {
                // Can't be legally cast — the card stays exiled (no second chance to play it).
                String logEntry = card.getName() + " has no valid targets and stays exiled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exile free-cast has no valid targets", gameData.id, card.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            // Remove from exile now that it will be cast; the ExileCastSpellTarget flow puts it on the stack.
            gameData.removeFromExile(exileCardId);
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");

            String logEntry = playerName + " plays " + card.getName()
                    + " without paying its mana cost — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.removeFromExile(exileCardId);
        gameData.stack.add(new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        String logEntry = playerName + " plays " + card.getName() + " without paying its mana cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} plays {} from exile without paying mana", gameData.id, playerName, card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
