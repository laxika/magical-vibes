package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ETBTokenTargetService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TargetLegalityService targetLegalityService;

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            PermanentChoiceContext.ETBSpellTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);

            List<UUID> validSpellCardIds = new ArrayList<>();
            for (StackEntry se : gameData.stack) {
                StackEntryType type = se.getEntryType();
                if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL
                        && type != StackEntryType.CREATURE_SPELL && type != StackEntryType.ENCHANTMENT_SPELL
                        && type != StackEntryType.ARTIFACT_SPELL && type != StackEntryType.PLANESWALKER_SPELL) {
                    continue;
                }
                if (pending.spellFilter() != null
                        && !targetLegalityService.matchesStackEntryPredicate(gameData, se, pending.spellFilter(), pending.controllerId())) {
                    continue;
                }
                validSpellCardIds.add(se.getCard().getId());
            }

            if (validSpellCardIds.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
                String etbLog = pending.sourceCard().getName() + "'s enter-the-battlefield ability has no valid spell targets.";
                gameBroadcastService.logAndBroadcast(gameData, etbLog);
                log.info("Game {} - {} ETB spell-target trigger skipped (no valid targets)", gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validSpellCardIds, List.of(),
                    pending.sourceCard().getName() + "'s ability — Choose target spell.");

            String logEntry = pending.sourceCard().getName() + "'s ETB ability triggers — choose a target spell.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} ETB spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextETBTokenTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)) {
            PermanentChoiceContext.ETBTokenTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);

            boolean canTargetPlayer = pending.effects().stream().anyMatch(CardEffect::canTargetPlayer);
            boolean canTargetPermanent = pending.effects().stream().anyMatch(CardEffect::canTargetPermanent);

            List<UUID> validPlayerTargets = new ArrayList<>();
            if (canTargetPlayer) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (matchesPlayerTargetFilter(pending.controllerId(), pid, pending.targetFilter())) {
                        validPlayerTargets.add(pid);
                    }
                }
            }

            List<UUID> validPermanentTargets = new ArrayList<>();
            if (canTargetPermanent) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (matchesPermanentTargetFilter(gameData, p, pending.targetFilter(), pending.effects())) {
                            validPermanentTargets.add(p.getId());
                        }
                    }
                }
            }

            if (validPlayerTargets.isEmpty() && validPermanentTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
                String etbLog = pending.sourceCard().getName() + "'s enter-the-battlefield ability has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, etbLog);
                log.info("Game {} - {} ETB token-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability — Choose a target.");

            log.info("Game {} - {} ETB token-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            PermanentChoiceContext.ETBTokenMultiTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
            Card card = pending.sourceCard();
            List<SpellTarget> groups = card.getSpellTargets();
            int idx = pending.currentGroupIndex();
            int chosenInGroup = pending.chosenInCurrentGroup();

            if (idx >= groups.size()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                pushMultiTargetETBStackEntry(gameData, pending);
                continue;
            }

            SpellTarget group = groups.get(idx);

            if (chosenInGroup >= group.getMaxTargets()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0));
                continue;
            }

            List<CardEffect> groupEffects = effectsForTargetGroup(card, pending.effects(), group.getIndex());

            // A target group whose bound effect was gated out (its intervening-if wasn't met as the
            // permanent entered, e.g. Noggle Hedge-Mage's Islands / Mountains ETBs) has no surviving
            // effect to target — advance past it without demanding targets, so a still-active later
            // group can still be chosen and the ability isn't wrongly skipped (CR 603.4).
            if (groupEffects.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0));
                continue;
            }

            boolean canTargetPlayer = groupEffects.stream().anyMatch(CardEffect::canTargetPlayer);
            boolean canTargetPermanent = groupEffects.stream().anyMatch(CardEffect::canTargetPermanent);

            List<UUID> validPlayerTargets = new ArrayList<>();
            if (canTargetPlayer) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (pending.chosenTargetsSoFar().contains(pid)) continue;
                    if (matchesPlayerTargetFilter(pending.controllerId(), pid, group.getFilter())) {
                        validPlayerTargets.add(pid);
                    }
                }
            }

            List<UUID> validPermanentTargets = new ArrayList<>();
            if (canTargetPermanent) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (pending.chosenTargetsSoFar().contains(p.getId())) continue;
                        if (matchesPermanentTargetFilter(gameData, p, group.getFilter(), groupEffects)) {
                            validPermanentTargets.add(p.getId());
                        }
                    }
                }
            }

            boolean noLegalTargets = validPlayerTargets.isEmpty() && validPermanentTargets.isEmpty();

            if (noLegalTargets) {
                if (chosenInGroup < group.getMinTargets()) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                    String etbLog = card.getName() + "'s enter-the-battlefield ability has no valid targets.";
                    gameBroadcastService.logAndBroadcast(gameData, etbLog);
                    log.info("Game {} - {} ETB multi-target trigger skipped (no valid targets for mandatory group {} at slot {})",
                            gameData.id, card.getName(), idx, chosenInGroup);
                    continue;
                }
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0));
                continue;
            }

            boolean minMet = chosenInGroup >= group.getMinTargets();
            if (minMet && !validPlayerTargets.contains(pending.controllerId())) {
                validPlayerTargets.add(pending.controllerId());
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            String slotLabel = "target " + (idx + 1)
                    + (group.getMaxTargets() > 1 ? "." + (chosenInGroup + 1) : "");
            String prompt = minMet
                    ? card.getName() + "'s ability — Choose " + slotLabel + " (or yourself to stop)."
                    : card.getName() + "'s ability — Choose " + slotLabel + ".";
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            log.info("Game {} - {} ETB multi-target trigger awaiting target (group {} slot {})",
                    gameData.id, card.getName(), idx, chosenInGroup);
            return;
        }
    }

    private void pushMultiTargetETBStackEntry(GameData gameData,
                                               PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        Card card = pending.sourceCard();
        StackEntry etbEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                pending.controllerId(),
                card.getName() + "'s ETB ability",
                new ArrayList<>(pending.effects()),
                0,
                null,
                pending.sourcePermanentId(),
                Map.of(),
                null,
                List.of(),
                new ArrayList<>(pending.chosenTargetsSoFar())
        );
        gameData.stack.add(etbEntry);
        String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, etbLog);
        log.info("Game {} - {} ETB multi-target ability pushed onto stack", gameData.id, card.getName());
    }

    boolean hasGroupWithMaxTargetsGreaterThanOne(Card card) {
        return card.getSpellTargets().stream().anyMatch(g -> g.getMaxTargets() > 1);
    }

    private List<CardEffect> effectsForTargetGroup(Card card, List<CardEffect> effects, int groupIndex) {
        List<CardEffect> matched = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (card.getEffectTargetIndex(effect) == groupIndex) {
                matched.add(effect);
            }
        }
        return matched;
    }

    private boolean matchesPlayerTargetFilter(UUID controllerId, UUID candidatePlayerId, TargetFilter targetFilter) {
        if (!(targetFilter instanceof PlayerPredicateTargetFilter playerFilter)) {
            return true;
        }
        PlayerPredicate predicate = playerFilter.predicate();
        if (predicate instanceof PlayerRelationPredicate relation) {
            return switch (relation.relation()) {
                case ANY -> true;
                case SELF -> controllerId != null && controllerId.equals(candidatePlayerId);
                case OPPONENT -> controllerId != null && !controllerId.equals(candidatePlayerId);
            };
        }
        return true;
    }

    private boolean matchesPermanentTargetFilter(GameData gameData, Permanent permanent,
                                                  TargetFilter targetFilter, List<CardEffect> effects) {
        if (targetFilter == null) {
            return gameQueryService.isCreature(gameData, permanent);
        }
        if (targetFilter instanceof PlayerPredicateTargetFilter) {
            return false;
        }
        return predicateEvaluationService.checkTargetFilter(targetFilter, permanent).isEmpty();
    }
}
