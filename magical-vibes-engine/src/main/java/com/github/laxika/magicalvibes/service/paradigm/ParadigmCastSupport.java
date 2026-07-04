package com.github.laxika.magicalvibes.service.paradigm;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;

    // @Lazy breaks cycle: ParadigmCastSupport → InputCompletionService → TurnProgressionService →
    // StepTriggerService → ParadigmService → ParadigmCastSupport.
    public ParadigmCastSupport(GameBroadcastService gameBroadcastService,
                               GameQueryService gameQueryService,
                               PredicateEvaluationService predicateEvaluationService,
                               GraveyardService graveyardService,
                               PlayerInputService playerInputService,
                               @Lazy TriggerCollectionService triggerCollectionService,
                               @Lazy InputCompletionService inputCompletionService) {
        this.gameBroadcastService = gameBroadcastService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.graveyardService = graveyardService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.inputCompletionService = inputCompletionService;
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

        StackEntryType spellType = mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        String playerName = player.getUsername();

        if (EffectResolution.needsTarget(card)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, card);

            if (validTargets.isEmpty()) {
                graveyardService.addCardToGraveyard(gameData, playerId, card);
                String logEntry = card.getName() + " has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} paradigm copy has no valid targets", gameData.id, card.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType, true));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + card.getName() + ".");

            String logEntry = playerName + " casts " + card.getName()
                    + " without paying its mana cost — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

        String logEntry = playerName + " casts " + card.getName() + " without paying its mana cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} casts {} paradigm copy without paying mana", gameData.id, playerName, card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private List<UUID> buildValidSpellTargets(GameData gameData, Card card) {
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        List<UUID> validTargets = new ArrayList<>();

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    if (card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        return validTargets;
    }

    private static StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }
}
