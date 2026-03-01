package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StackResolutionService {

    private final GameHelper gameHelper;
    private final LegendRuleService legendRuleService;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;
    private final GameBroadcastService gameBroadcastService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;

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
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
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

        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(card));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, entry.getTargetPermanentId(), true);
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
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
                gameHelper.putPermanentOntoBattlefield(gameData, controllerId, perm);

                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);

                // Handle control-changing auras (e.g., Persuasion)
                boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    creatureControlService.stealPermanent(gameData, controllerId, target);
                }

                // Process aura ETB effects (e.g., Volition Reins)
                if (!gameData.interaction.isAwaitingInput()) {
                    gameHelper.processCreatureETBEffects(gameData, controllerId, card, entry.getTargetPermanentId(), true);
                }
            }
        } else {
            gameHelper.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(card));

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

            // Process general ETB effects (e.g., token creation for Kindred Enchantments)
            if (!gameData.interaction.isAwaitingInput()) {
                gameHelper.processCreatureETBEffects(gameData, controllerId, card, null, true);
            }

            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }
        }
    }

    private void resolveArtifactSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        if (gameHelper.prepareCloneReplacementEffect(gameData, controllerId, card, entry.getTargetPermanentId())) {
            return;
        }

        // "As enters" card name choice (e.g. Pithing Needle) — name must be chosen
        // BEFORE the permanent enters the battlefield (MTG Rule 614.1c)
        boolean needsCardNameChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseCardNameEffect);
        if (needsCardNameChoice) {
            playerInputService.beginCardNameChoice(gameData, controllerId, card);
            return;
        }

        Permanent perm = new Permanent(card);

        // "Enters with X charge counters" — replacement effect (MTG Rule 614.1c)
        boolean hasXChargeCounterEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof EnterWithXChargeCountersEffect);
        if (hasXChargeCounterEffect) {
            perm.setChargeCounters(entry.getXValue());
        }

        // "Enters with N charge counters" — replacement effect for fixed count (MTG Rule 614.1c)
        int fixedChargeCounters = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof EnterWithFixedChargeCountersEffect)
                .map(e -> ((EnterWithFixedChargeCountersEffect) e).count())
                .findFirst().orElse(0);
        if (fixedChargeCounters > 0) {
            perm.setChargeCounters(fixedChargeCounters);
        }

        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        if (hasXChargeCounterEffect && entry.getXValue() > 0) {
            String logEntry = card.getName() + " enters the battlefield with " + entry.getXValue() + " charge counters under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (fixedChargeCounters > 0) {
            String logEntry = card.getName() + " enters the battlefield with " + fixedChargeCounters + " charge counters under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

        // Process ETB effects for all artifacts (creature and non-creature)
        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, entry.getTargetPermanentId(), true);

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void resolvePlaneswalkerSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(card.getLoyalty() != null ? card.getLoyalty() : 0);
        perm.setSummoningSick(false);
        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield with " + perm.getLoyaltyCounters() + " loyalty under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void resolveSpellOrAbility(GameData gameData, StackEntry entry) {
        // Check if targeted spell/ability fizzles due to illegal target
        boolean targetFizzled = targetLegalityService.isTargetIllegalOnResolution(gameData, entry);

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
                if (entry.isReturnToHandAfterResolving()) {
                    List<Card> hand = gameData.playerHands.get(entry.getControllerId());
                    hand.add(entry.getCard());
                    String returnLog = entry.getCard().getName() + " is returned to its owner's hand.";
                    gameBroadcastService.logAndBroadcast(gameData, returnLog);
                } else if (gameData.pendingReturnToHandOnDiscardType != null) {
                    // Spell disposition deferred — will be resolved after the async discard
                    // completes (e.g. Psychic Miasma: goes to hand if a land is discarded,
                    // otherwise to graveyard).
                } else if (entry.getEffectsToResolve().stream()
                        .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect)) {
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

}


