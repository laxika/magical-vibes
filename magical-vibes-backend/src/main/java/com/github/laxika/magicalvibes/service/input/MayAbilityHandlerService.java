package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayAbilityHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (gameData.awaitingInput != AwaitingInput.MAY_ABILITY_CHOICE) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        if (!player.getId().equals(gameData.awaitingMayAbilityPlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.awaitingInput = null;
        gameData.awaitingMayAbilityPlayerId = null;

        // Counter-unless-pays — handled via the may ability system
        boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
        if (isCounterUnlessPays) {
            handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-discard — handled via the may ability system
        boolean isSacrificeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect);
        if (isSacrificeUnlessDiscard) {
            handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy spell retarget — choose new targets for a copied spell
        boolean isCopySpellRetarget = ability.effects().stream().anyMatch(e -> e instanceof CopySpellEffect);
        if (isCopySpellRetarget) {
            handleCopySpellRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // Clone copy creature effect — handled as replacement effect (pre-entry)
        boolean isCloneCopy = ability.effects().stream().anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
        if (isCloneCopy) {
            if (accepted) {
                // Collect valid creature targets (Clone is NOT on the battlefield yet)
                List<UUID> creatureIds = new ArrayList<>();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (gameQueryService.isCreature(gameData, p)) {
                            creatureIds.add(p.getId());
                        }
                    }
                }
                playerInputService.beginPermanentChoice(gameData, ability.controllerId(), creatureIds, "Choose a creature to copy.");

                String logEntry = player.getUsername() + " accepts — choosing a creature to copy.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts clone copy", gameData.id, player.getUsername());
            } else {
                gameData.permanentChoiceContext = null;
                String logEntry = player.getUsername() + " declines to copy a creature. Clone enters as 0/0.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines clone copy", gameData.id, player.getUsername());

                gameHelper.completeCloneEntry(gameData, null);
                gameHelper.performStateBasedActions(gameData);

                if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                    gameHelper.processNextDeathTriggerTarget(gameData);
                    if (gameData.awaitingInput != null) {
                        return;
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                    return;
                }

                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects())
            ));

            String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        int amount = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessPaysEffect)
                .map(e -> ((CounterUnlessPaysEffect) e).amount())
                .findFirst().orElse(0);
        UUID targetCardId = ability.targetCardId();

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted) {
            ManaCost cost = new ManaCost("{" + amount + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + amount + "}. " + targetEntry.getCard().getName() + " is not countered.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pays {} to avoid counter", gameData.id, player.getUsername(), amount);
            } else {
                gameData.stack.remove(targetEntry);
                gameHelper.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getCard());
                String logEntry = player.getUsername() + " can't pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} can't pay {} — spell countered", gameData.id, player.getUsername(), amount);
            }
        } else {
            gameData.stack.remove(targetEntry);
            gameHelper.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getCard());
            String logEntry = player.getUsername() + " declines to pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to pay {} — spell countered", gameData.id, player.getUsername(), amount);
        }

        gameHelper.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleSacrificeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SacrificeUnlessDiscardCardTypeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect)
                .map(e -> (SacrificeUnlessDiscardCardTypeEffect) e)
                .findFirst().orElseThrow();

        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        if (accepted) {
            // Per ruling 2008-04-01: player may still discard even if the creature
            // is no longer on the battlefield.
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (hand.get(i).getType() == effect.requiredType()) {
                        validIndices.add(i);
                    }
                }
            }

            if (!validIndices.isEmpty()) {
                String typeName = effect.requiredType().name().toLowerCase();
                gameData.discardCausedByOpponent = false;
                gameData.awaitingDiscardRemainingCount = 1;
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " card to discard.");

                String logEntry = player.getUsername() + " chooses to discard a " + typeName + " card.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            gameHelper.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to discard. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        gameHelper.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleCopySpellRetargetChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            String logEntry = player.getUsername() + " keeps the original targets for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to retarget copy", gameData.id, player.getUsername());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Find the copy on the stack
        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }

        if (copyEntry == null) {
            log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        Card copiedCard = copyEntry.getCard();
        List<UUID> validTargets = new ArrayList<>();

        if (copiedCard.isNeedsSpellTarget()) {
            // Targets a spell on the stack
            SpellTypeTargetFilter spellFilter = copiedCard.getTargetFilter() instanceof SpellTypeTargetFilter stf ? stf : null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(copyCardId)) continue; // exclude the copy itself
                if (spellFilter != null && !spellFilter.spellTypes().contains(se.getEntryType())) continue;
                validTargets.add(se.getCard().getId());
            }
        } else if (copiedCard.isNeedsTarget()) {
            List<CardEffect> effects = copyEntry.getEffectsToResolve();

            // Check if it targets a player only
            boolean targetsPlayer = isPlayerTargetingEffects(effects);
            if (targetsPlayer) {
                validTargets.addAll(gameData.orderedPlayerIds);
            } else {
                // Determine targeting category from effects
                boolean targetsAnyPermanent = false;
                boolean targetsEnchantmentOnly = false;
                boolean targetsCreatureOrPlayer = false;
                boolean requiresAttacking = false;
                Set<CardType> configuredTargetTypes = null;

                for (CardEffect effect : effects) {
                    if (effect instanceof ReturnTargetPermanentToHandEffect) {
                        targetsAnyPermanent = true;
                        break;
                    }
                    if (effect instanceof GainControlOfTargetAuraEffect) {
                        targetsEnchantmentOnly = true;
                        break;
                    }
                    if (effect instanceof DealXDamageToAnyTargetAndGainXLifeEffect
                            || effect instanceof PreventDamageToTargetEffect) {
                        targetsCreatureOrPlayer = true;
                        break;
                    }
                    if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                        requiresAttacking = true;
                        break;
                    }
                    if (effect instanceof DestroyTargetPermanentEffect dte) {
                        configuredTargetTypes = dte.targetTypes();
                        break;
                    }
                    if (effect instanceof TapOrUntapTargetPermanentEffect toue) {
                        configuredTargetTypes = toue.allowedTypes();
                        break;
                    }
                    if (effect instanceof TapTargetPermanentEffect tpe) {
                        configuredTargetTypes = tpe.allowedTypes();
                        break;
                    }
                }

                // "Any target" spells can also target players
                if (targetsCreatureOrPlayer) {
                    validTargets.addAll(gameData.orderedPlayerIds);
                }

                // Add matching permanents from all battlefields
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (targetsAnyPermanent) {
                            validTargets.add(p.getId());
                        } else if (targetsEnchantmentOnly) {
                            if (p.getCard().getType() == CardType.ENCHANTMENT && p.getAttachedTo() != null) {
                                validTargets.add(p.getId());
                            }
                        } else if (configuredTargetTypes != null) {
                            if (configuredTargetTypes.contains(p.getCard().getType())) {
                                validTargets.add(p.getId());
                            }
                        } else if (requiresAttacking) {
                            if (gameQueryService.isCreature(gameData, p) && p.isAttacking()) {
                                validTargets.add(p.getId());
                            }
                        } else {
                            // Default: creature targeting (including "any target" which also adds players above)
                            if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = "No valid targets available for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - No valid targets for copy retarget", gameData.id);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        gameData.permanentChoiceContext = new PermanentChoiceContext.CopySpellRetarget(copyCardId);
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copiedCard.getName() + ".");
    }

    private boolean isPlayerTargetingEffects(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ChooseCardsFromTargetHandToTopOfLibraryEffect
                    || effect instanceof DoubleTargetPlayerLifeEffect
                    || effect instanceof ExtraTurnEffect
                    || effect instanceof LookAtHandEffect
                    || effect instanceof MillTargetPlayerEffect
                    || effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect
                    || effect instanceof ShuffleGraveyardIntoLibraryEffect
                    || effect instanceof TargetPlayerLosesLifeAndControllerGainsLifeEffect) {
                return true;
            }
        }
        return false;
    }
}
