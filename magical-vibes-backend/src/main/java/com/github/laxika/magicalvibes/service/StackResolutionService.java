package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StackResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInputService playerInputService;

    public void resolveTopOfStack(GameData gameData) {
        if (gameData.stack.isEmpty()) return;

        StackEntry entry = gameData.stack.removeLast();
        gameData.priorityPassedBy.clear();

        if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
            resolveCreatureSpell(gameData, entry);
        } else if (entry.getEntryType() == StackEntryType.ENCHANTMENT_SPELL) {
            resolveEnchantmentSpell(gameData, entry);
        } else if (entry.getEntryType() == StackEntryType.ARTIFACT_SPELL) {
            resolveArtifactSpell(gameData, entry);
        } else if (entry.getEntryType() == StackEntryType.PLANESWALKER_SPELL) {
            resolvePlaneswalkerSpell(gameData, entry);
        } else if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL
                || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
            resolveSpellOrAbility(gameData, entry);
        }

        // If the ETB handler already set up a user interaction (e.g. Clone copy choice),
        // skip post-resolution SBA — the creature must remain alive until the choice resolves.
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Check SBA after resolution — creatures may have 0 toughness from effects (e.g. -1/-1)
        gameHelper.performStateBasedActions(gameData);

        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            gameHelper.processNextDiscardSelfTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameBroadcastService.broadcastGameState(gameData);
    }

    private void resolveCreatureSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        if (gameHelper.prepareCloneReplacementEffect(gameData, controllerId, card, entry.getTargetPermanentId())) {
            return;
        }

        gameData.playerBattlefields.get(controllerId).add(new Permanent(card));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, entry.getTargetPermanentId());
        if (!gameData.interaction.isAwaitingInput()) {
            gameHelper.checkLegendRule(gameData, controllerId);
        }
    }

    private void resolveEnchantmentSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        // Aura fizzles if its target is no longer on the battlefield
        if (card.isAura() && entry.getTargetPermanentId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
            if (target == null) {
                String fizzleLog = card.getName() + " fizzles (enchanted creature no longer exists).";
                gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                gameHelper.addCardToGraveyard(gameData, controllerId, card);

                log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetPermanentId());
            } else {
                Permanent perm = new Permanent(card);
                perm.setAttachedTo(entry.getTargetPermanentId());
                gameData.playerBattlefields.get(controllerId).add(perm);

                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);

                // Handle control-changing auras (e.g., Persuasion)
                boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    gameHelper.stealCreature(gameData, controllerId, target);
                }
            }
        } else {
            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

            // Check if enchantment has "as enters" color choice
            boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof ChooseColorEffect);
            if (needsColorChoice) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        }
    }

    private void resolveArtifactSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        gameData.playerBattlefields.get(controllerId).add(new Permanent(card));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
        if (!gameData.interaction.isAwaitingInput()) {
            gameHelper.checkLegendRule(gameData, controllerId);
        }
    }

    private void resolvePlaneswalkerSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(card.getLoyalty() != null ? card.getLoyalty() : 0);
        perm.setSummoningSick(false);
        gameData.playerBattlefields.get(controllerId).add(perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield with " + perm.getLoyaltyCounters() + " loyalty under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
        if (!gameData.interaction.isAwaitingInput()) {
            gameHelper.checkLegendRule(gameData, controllerId);
        }
    }

    private void resolveSpellOrAbility(GameData gameData, StackEntry entry) {
        // Check if targeted spell/ability fizzles due to illegal target
        boolean targetFizzled = checkTargetFizzle(gameData, entry);

        if (targetFizzled) {
            String fizzleLog = entry.getDescription() + " fizzles (illegal target).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} fizzles, target {} is illegal",
                    gameData.id, entry.getDescription(), entry.getTargetPermanentId());

            // Fizzled spells still go to graveyard (copies cease to exist per rule 707.10a)
            if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                    || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                    && !entry.isCopy()) {
                gameHelper.addCardToGraveyard(gameData, entry.getControllerId(), entry.getCard());
            }
        } else {
            String logEntry = entry.getDescription() + " resolves.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

            effectResolutionService.resolveEffects(gameData, entry);

            // Rule 723.1b: "End the turn" exiles the resolving spell itself (copies cease to exist per rule 707.10a)
            if (gameData.endTurnRequested) {
                gameData.endTurnRequested = false;
                if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                        && !entry.isCopy()) {
                    gameData.playerExiledCards.get(entry.getControllerId()).add(entry.getCard());
                }
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }

            // Copies cease to exist per rule 707.10a — skip graveyard/shuffle
            if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                    || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                    && !entry.isCopy()) {
                boolean shuffled = entry.getEffectsToResolve().stream()
                        .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect);
                if (shuffled) {
                    // Ensure the card is shuffled into library even when an earlier effect
                    // required user input and broke the effect resolution loop before
                    // the ShuffleIntoLibraryEffect handler could run.
                    List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
                    if (!deck.contains(entry.getCard())) {
                        deck.add(entry.getCard());
                        Collections.shuffle(deck);
                        String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
                        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
                    }
                } else {
                    gameHelper.addCardToGraveyard(gameData, entry.getControllerId(), entry.getCard());
                }
            }
        }
    }

    private boolean checkTargetFizzle(GameData gameData, StackEntry entry) {
        // Non-targeting abilities (e.g. "destroy that creature and Loyal Sentry") reference
        // permanents without using the "target" keyword — they resolve even if the referenced
        // permanent is gone (per MTG rule 608.2b: only check legality for actual targets).
        if (entry.isNonTargeting()) {
            return false;
        }

        boolean targetFizzled = false;

        if (entry.getTargetPermanentId() != null) {
            if (entry.getTargetZone() == TargetZone.GRAVEYARD) {
                targetFizzled = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId()) == null;
            } else if (entry.getTargetZone() == TargetZone.STACK) {
                targetFizzled = gameData.stack.stream()
                        .noneMatch(se -> se.getCard().getId().equals(entry.getTargetPermanentId()));
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
                if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetPermanentId())) {
                    targetFizzled = true;
                } else if (targetPerm != null && entry.getCard() != null && entry.getCard().getTargetFilter() != null) {
                    try {
                        gameQueryService.validateTargetFilter(entry.getCard().getTargetFilter(), targetPerm);
                    } catch (IllegalStateException e) {
                        targetFizzled = true;
                    }
                }
            }
        }

        // Check multi-target permanent fizzle: if ALL targeted permanents/players are gone, fizzle
        if (!targetFizzled && entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
            boolean allGone = true;
            for (UUID permId : entry.getTargetPermanentIds()) {
                if (gameQueryService.findPermanentById(gameData, permId) != null
                        || gameData.playerIds.contains(permId)) {
                    allGone = false;
                    break;
                }
            }
            if (allGone) {
                targetFizzled = true;
            }
        }

        // Check multi-target graveyard fizzle: if ALL targeted cards are gone, fizzle
        if (!targetFizzled && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            boolean allGone = true;
            for (UUID cardId : entry.getTargetCardIds()) {
                if (gameQueryService.findCardInGraveyardById(gameData, cardId) != null) {
                    allGone = false;
                    break;
                }
            }
            if (allGone) {
                targetFizzled = true;
            }
        }

        return targetFizzled;
    }
}


