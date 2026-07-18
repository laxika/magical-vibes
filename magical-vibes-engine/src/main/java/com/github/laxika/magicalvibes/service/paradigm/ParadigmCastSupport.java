package com.github.laxika.magicalvibes.service.paradigm;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
 * Casts an exiled Paradigm copy without paying its mana cost (timing restrictions ignored per
 * paradigm reminder text for copies cast at the beginning of the first main phase).
 */
@Slf4j
@Component
public class ParadigmCastSupport {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;

    // @Lazy breaks cycle: ParadigmCastSupport → InputCompletionService → TurnProgressionService →
    // StepTriggerService → ParadigmService → ParadigmCastSupport.
    public ParadigmCastSupport(GameBroadcastService gameBroadcastService,
                               GraveyardService graveyardService,
                               PlayerInputService playerInputService,
                               @Lazy TriggerCollectionService triggerCollectionService,
                               @Lazy InputCompletionService inputCompletionService,
                               ExileCastTargetSupport exileCastTargetSupport) {
        this.gameBroadcastService = gameBroadcastService;
        this.graveyardService = graveyardService;
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
        gameData.removeFromExile(exileCardId);

        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        String playerName = player.getUsername();

        if (EffectResolution.needsTarget(card)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
            boolean multiTarget = card.getMaxTargets() > 1;
            // Multi-target: require a full legal set of targets (CR 601.2c) before prompting.
            boolean hasLegalTargets = multiTarget
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, playerId)
                    : !firstCandidates.isEmpty();

            if (!hasLegalTargets) {
                // Single-target keeps the historical behavior of putting the copy into the graveyard.
                // A multi-target copy that can't be legally cast ceases to exist instead (CR 707.10a).
                if (!multiTarget) {
                    graveyardService.addCardToGraveyard(gameData, playerId, card);
                }
                String logEntry = card.getName() + " has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " has no valid targets."));
                log.info("Game {} - {} paradigm copy has no valid targets", gameData.id, card.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType, true));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");

            String logEntry = playerName + " casts " + card.getName()
                    + " without paying its mana cost — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " has no valid targets."));
            return;
        }

        // The Paradigm copy must cease to exist on resolution (CR 707.10a), not go to a zone.
        StackEntry copyEntry = new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        );
        copyEntry.setCopy(true);
        gameData.stack.add(copyEntry);

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " casts " , card, " without paying its mana cost."));
        log.info("Game {} - {} casts {} paradigm copy without paying mana", gameData.id, playerName, card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
