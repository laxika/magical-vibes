package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
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
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayAbilityHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final StateBasedActionService stateBasedActionService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final TargetLegalityService targetLegalityService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        InteractionContext.MayAbilityChoice mayAbilityChoice = gameData.interaction.mayAbilityChoiceContext();
        if (mayAbilityChoice == null || !player.getId().equals(mayAbilityChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMayAbilityChoice();

        // Cast-from-library-without-paying — e.g. Galvanoth (second phase: cast prompt)
        // The first may (look at top card) has the source creature as sourceCard,
        // the second may (cast the spell) has the instant/sorcery as sourceCard.
        // We only intercept the second phase — the first goes through the generic path
        // so the CastTopOfLibraryWithoutPayingManaCostEffect resolves via @HandlesEffect.
        CastTopOfLibraryWithoutPayingManaCostEffect castFromLibEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTopOfLibraryWithoutPayingManaCostEffect)
                .map(e -> (CastTopOfLibraryWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (castFromLibEffect != null && castFromLibEffect.castableTypes().contains(ability.sourceCard().getType())) {
            handleCastFromLibraryChoice(gameData, player, accepted, ability);
            return;
        }

        // May-not-untap choice from untap step (e.g. Rust Tick)
        boolean isMayNotUntap = ability.effects().stream().anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
        if (isMayNotUntap) {
            handleMayNotUntapChoice(gameData, player, accepted, ability);
            return;
        }

        // Counter-unless-pays — handled via the may ability system
        boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
        if (isCounterUnlessPays) {
            handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Lose-life-unless-discard — handled via the may ability system
        boolean isLoseLifeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof LoseLifeUnlessDiscardEffect);
        if (isLoseLifeUnlessDiscard) {
            handleLoseLifeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Opponent may return exiled card to hand, or controller draws N (e.g. Distant Memories)
        OpponentMayReturnExiledCardOrDrawEffect opponentExileChoice = ability.effects().stream()
                .filter(e -> e instanceof OpponentMayReturnExiledCardOrDrawEffect)
                .map(e -> (OpponentMayReturnExiledCardOrDrawEffect) e)
                .findFirst().orElse(null);
        if (opponentExileChoice != null) {
            handleOpponentExileChoice(gameData, player, accepted, ability, opponentExileChoice);
            return;
        }

        // Sacrifice-unless-discard — handled via the may ability system
        boolean isSacrificeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect);
        if (isSacrificeUnlessDiscard) {
            handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-return-own-permanent — handled via the may ability system
        boolean isSacrificeUnlessReturnPermanent = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessReturnOwnPermanentTypeToHandEffect);
        if (isSacrificeUnlessReturnPermanent) {
            handleSacrificeUnlessReturnOwnPermanentChoice(gameData, player, accepted, ability);
            return;
        }

        // Generic single-draw replacement
        ReplaceSingleDrawEffect replaceSingleDrawEffect = ability.effects().stream()
                .filter(e -> e instanceof ReplaceSingleDrawEffect)
                .map(e -> (ReplaceSingleDrawEffect) e)
                .findFirst()
                .orElse(null);
        if (replaceSingleDrawEffect != null) {
            handleSingleDrawReplacementChoice(gameData, player, accepted, ability, replaceSingleDrawEffect);
            return;
        }

        // Copy spell retarget — choose new targets for a copied spell
        boolean isCopySpellRetarget = ability.effects().stream().anyMatch(e -> e instanceof CopySpellEffect);
        if (isCopySpellRetarget) {
            handleCopySpellRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // BecomeCopyOfTargetCreatureEffect — targets "another creature" (e.g. Cryptoplasm)
        boolean isBecomeCopyEffect = ability.effects().stream().anyMatch(e -> e instanceof BecomeCopyOfTargetCreatureEffect);
        if (isBecomeCopyEffect) {
            handleBecomeCopyChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy permanent effect (Clone / Sculpting Steel) — handled as replacement effect (pre-entry)
        CopyPermanentOnEnterEffect copyEffect = ability.effects().stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect != null) {
            String typeLabel = copyEffect.typeLabel();
            if (accepted) {
                // Collect valid targets (the copying permanent is NOT on the battlefield yet)
                List<UUID> validIds = new ArrayList<>();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (gameQueryService.matchesPermanentPredicate(gameData, p, copyEffect.filter())) {
                            validIds.add(p.getId());
                        }
                    }
                }
                playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validIds, "Choose a " + typeLabel + " to copy.");

                String logEntry = player.getUsername() + " accepts — choosing a " + typeLabel + " to copy.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts copy {}", gameData.id, player.getUsername(), typeLabel);
            } else {
                gameData.interaction.clearPermanentChoiceContext();
                String logEntry = player.getUsername() + " declines to copy a " + typeLabel + ". " + ability.sourceCard().getName() + " enters without copying.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines copy {}", gameData.id, player.getUsername(), typeLabel);

                gameHelper.completeCloneEntry(gameData, null);
                stateBasedActionService.performStateBasedActions(gameData);

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

                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Sacrifice-artifact for divided damage (e.g. Kuldotha Flamefiend)
        boolean isSacrificeArtifact = ability.effects().stream()
                .anyMatch(e -> e instanceof SacrificeArtifactThenDealDividedDamageEffect);
        if (isSacrificeArtifact) {
            handleMaySacrificeArtifactForDividedDamage(gameData, player, accepted, ability);
            return;
        }

        // Mana payment for may-pay triggers (e.g. Embersmith "pay {1}", Vigil for the Lost "pay {X}")
        int xValuePaid = 0;
        if (accepted && ability.manaCost() != null) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());

            if (cost.hasX()) {
                // X cost: pay all available mana as X
                int maxX = cost.calculateMaxX(pool);
                if (maxX <= 0) {
                    String logEntry = player.getUsername() + " has no mana to pay for " + ability.sourceCard().getName() + "'s ability.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no mana for X may ability", gameData.id, player.getUsername());

                    playerInputService.processNextMayAbility(gameData);
                    if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                        gameData.priorityPassedBy.clear();
                        gameBroadcastService.broadcastGameState(gameData);
                        turnProgressionService.resolveAutoPass(gameData);
                    }
                    return;
                }
                xValuePaid = maxX;
                cost.pay(pool, maxX);
            } else {
                if (!cost.canPay(pool)) {
                    String logEntry = player.getUsername() + " cannot pay " + ability.manaCost() + " for " + ability.sourceCard().getName() + "'s ability.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} can't pay {} for may ability", gameData.id, player.getUsername(), ability.manaCost());

                    playerInputService.processNextMayAbility(gameData);
                    if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                        gameData.priorityPassedBy.clear();
                        gameBroadcastService.broadcastGameState(gameData);
                        turnProgressionService.resolveAutoPass(gameData);
                    }
                    return;
                }
                cost.pay(pool);
            }
        }

        // Targeted may ability (e.g. "you may deal 3 damage to target creature", "you may destroy target Equipment")
        boolean isTargetedPermanentEffect = ability.effects().stream()
                .anyMatch(CardEffect::canTargetPermanent);

        if (accepted && isTargetedPermanentEffect) {
            handleTargetedMayAbilityAccepted(gameData, player, ability);
            return;
        }

        if (accepted) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    xValuePaid
            );

            // Self-targeting effects need the source permanent's ID to resolve
            boolean needsSelfTarget = ability.effects().stream().anyMatch(e ->
                    e instanceof PutChargeCounterOnSelfEffect
                            || e instanceof AnimateSelfEffect || e instanceof AnimateSelfByChargeCountersEffect
                            || e instanceof AnimateSelfWithStatsEffect || e instanceof BoostSelfEffect
                            || e instanceof ImprintDyingCreatureEffect
                            || e instanceof ExileFromHandToImprintEffect
                            || e instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect);
            if (needsSelfTarget) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard() == ability.sourceCard()) {
                            entry.setTargetPermanentId(p.getId());
                            break;
                        }
                    }
                }
            }

            gameData.stack.add(entry);

            String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleTargetedMayAbilityAccepted(GameData gameData, Player player, PendingMayAbility ability) {
        // Collect valid permanent targets from all battlefields using card's target filter
        List<UUID> validTargets = new ArrayList<>();
        Card sourceCard = ability.sourceCard();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (sourceCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                    if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                        validTargets.add(p.getId());
                    }
                } else if (gameQueryService.isCreature(gameData, p)) {
                    validTargets.add(p.getId());
                }
            }
        }

        // Add player IDs for effects that can target players (e.g. DealDamageToAnyTargetEffect)
        boolean canTargetPlayer = ability.effects().stream().anyMatch(CardEffect::canTargetPlayer);
        if (canTargetPlayer) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        if (validTargets.isEmpty()) {
            String logEntry = ability.sourceCard().getName() + "'s ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} may ability has no valid targets", gameData.id, ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects())
        ));
        String targetDescription;
        if (sourceCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
            targetDescription = filter.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayer) {
            targetDescription = "any target";
        } else {
            targetDescription = "creature";
        }
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                ability.sourceCard().getName() + "'s ability — Choose target " + targetDescription + ".");

        String logEntry = player.getUsername() + " accepts — choosing a target for " + ability.sourceCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} accepts targeted may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
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
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
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

        stateBasedActionService.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
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
                    if (effect.requiredType() == null || hand.get(i).getType() == effect.requiredType()) {
                        validIndices.add(i);
                    }
                }
            }

            if (!validIndices.isEmpty()) {
                String typeName = effect.requiredType() == null ? "card" : effect.requiredType().name().toLowerCase() + " card";
                gameData.discardCausedByOpponent = false;
                gameData.interaction.setDiscardRemainingCount(1);
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " to discard.");

                String logEntry = player.getUsername() + " chooses to discard a " + typeName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to discard. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        stateBasedActionService.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleLoseLifeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LoseLifeUnlessDiscardEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LoseLifeUnlessDiscardEffect)
                .map(e -> (LoseLifeUnlessDiscardEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();

        if (accepted) {
            List<Card> hand = gameData.playerHands.get(targetPlayerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    validIndices.add(i);
                }
            }

            if (!validIndices.isEmpty()) {
                gameData.discardCausedByOpponent = false;
                gameData.interaction.setDiscardRemainingCount(1);
                playerInputService.beginDiscardChoice(gameData, targetPlayerId, validIndices,
                        "Choose a card to discard.");

                String logEntry = player.getUsername() + " chooses to discard a card.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + "'s life total can't change.");
        } else {
            int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
            gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());

            String logEntry = player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} loses {} life (declined discard, {})", gameData.id, player.getUsername(), effect.lifeLoss(), ability.sourceCard().getName());
        }

        stateBasedActionService.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleOpponentExileChoice(GameData gameData, Player player, boolean accepted,
                                             PendingMayAbility ability, OpponentMayReturnExiledCardOrDrawEffect effect) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID exiledCardId = ability.targetCardId();

        // Find the spell controller (the other player)
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }

        if (controllerId == null) {
            throw new IllegalStateException("Cannot find exiled card owner");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (accepted) {
            // Opponent lets the controller have the exiled card — move from exile to hand
            Card exiledCard = null;
            List<Card> exileZone = gameData.playerExiledCards.get(controllerId);
            if (exileZone != null) {
                for (int i = 0; i < exileZone.size(); i++) {
                    if (exileZone.get(i).getId().equals(exiledCardId)) {
                        exiledCard = exileZone.remove(i);
                        break;
                    }
                }
            }

            if (exiledCard != null) {
                gameData.playerHands.get(controllerId).add(exiledCard);
                String logEntry = opponentName + " allows it. " + controllerName + " puts " + exiledCard.getName() + " into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} allows exile return, {} gets {}", gameData.id, opponentName, controllerName, exiledCard.getName());
            }
        } else {
            // Opponent declines — controller draws cards
            int drawCount = effect.drawCount();
            for (int i = 0; i < drawCount; i++) {
                gameHelper.resolveDrawCard(gameData, controllerId);
            }

            String logEntry = opponentName + " declines. " + controllerName + " draws " + drawCount + " cards.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines exile return, {} draws {}", gameData.id, opponentName, controllerName, drawCount);
        }

        stateBasedActionService.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleSacrificeUnlessReturnOwnPermanentChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessReturnOwnPermanentTypeToHandEffect)
                .map(e -> (SacrificeUnlessReturnOwnPermanentTypeToHandEffect) e)
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
            // Collect valid permanent IDs of the required type
            List<UUID> validIds = new ArrayList<>();
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().getType() == effect.permanentType()
                            || p.getCard().getAdditionalTypes().contains(effect.permanentType())) {
                        validIds.add(p.getId());
                    }
                }
            }

            if (!validIds.isEmpty()) {
                String typeName = effect.permanentType().name().toLowerCase();
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.BounceOwnPermanentOrSacrificeSelf(controllerId, sourceCard.getId()));
                playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                        "Choose an " + typeName + " to return to hand.");

                String logEntry = player.getUsername() + " chooses to return an " + typeName + " to hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts sacrifice-unless-return for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Battlefield changed since trigger — no valid permanents left, fall through to sacrifice
        }

        // Declined or no valid permanents left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to return a permanent. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to return a permanent.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        stateBasedActionService.performStateBasedActions(gameData);
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
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
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
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
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
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
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(copyCardId)) continue; // exclude the copy itself
                try {
                    targetLegalityService.validateSpellTargetOnStack(gameData, se.getCard().getId(), copiedCard.getTargetFilter(), copyEntry.getControllerId());
                    validTargets.add(se.getCard().getId());
                } catch (IllegalStateException ignored) {
                    // Invalid target for copied spell filter; skip.
                }
            }
        } else if (copiedCard.isNeedsTarget()) {
            Zone targetZone = copyEntry.getTargetZone() != null ? copyEntry.getTargetZone() : Zone.BATTLEFIELD;

            List<UUID> candidateTargets = new ArrayList<>(gameData.orderedPlayerIds);
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    candidateTargets.add(p.getId());
                }
            }

            for (UUID candidate : candidateTargets) {
                try {
                    targetLegalityService.validateSpellTargeting(
                            gameData,
                            copiedCard,
                            candidate,
                            targetZone,
                            copyEntry.getControllerId()
                    );
                    validTargets.add(candidate);
                } catch (IllegalStateException ignored) {
                    // Candidate is not legal for this copied spell.
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = "No valid targets available for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - No valid targets for copy retarget", gameData.id);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(copyCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copiedCard.getName() + ".");
    }

    private void handleSingleDrawReplacementChoice(GameData gameData, Player player, boolean accepted,
                                                   PendingMayAbility ability,
                                                   ReplaceSingleDrawEffect effect) {
        UUID drawingPlayerId = effect.playerId();
        String playerName = gameData.playerIdToName.get(drawingPlayerId);

        if (!accepted) {
            gameHelper.resolveDrawCardWithoutStaticReplacementCheck(gameData, drawingPlayerId);
            String logEntry = player.getUsername() + " declines to use " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ABUNDANCE) {
            gameData.interaction.beginColorChoice(
                    drawingPlayerId,
                    null,
                    null,
                    new com.github.laxika.magicalvibes.model.ColorChoiceContext.DrawReplacementChoice(drawingPlayerId, effect.kind())
            );
            sessionManager.sendToPlayer(drawingPlayerId, new ChooseColorMessage(
                    List.of("LAND", "NONLAND"),
                    "Choose land or nonland for Abundance."
            ));
            log.info("Game {} - Awaiting {} to choose land or nonland for Abundance", gameData.id, playerName);
            return;
        }

        throw new IllegalStateException("Unsupported draw replacement kind: " + effect.kind());
    }

    private void handleBecomeCopyChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();

        if (!accepted) {
            String logEntry = player.getUsername() + " declines " + sourceCard.getName() + "'s copy ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines become-copy ability from {}", gameData.id, player.getUsername(), sourceCard.getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Find source permanent by card identity
        Permanent sourcePermanent = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard() == sourceCard) {
                    sourcePermanent = p;
                    break;
                }
            }
            if (sourcePermanent != null) break;
        }

        if (sourcePermanent == null) {
            String logEntry = sourceCard.getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} become-copy source no longer on battlefield", gameData.id, sourceCard.getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Find target permanent (stored in targetCardId during resolution queueing)
        UUID targetPermId = ability.targetCardId();
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermId);
        if (targetPerm == null) {
            String logEntry = sourceCard.getName() + "'s copy target is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} become-copy target no longer on battlefield", gameData.id, sourceCard.getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        // Apply the copy
        String originalName = sourcePermanent.getCard().getName();
        gameHelper.applyCloneCopy(sourcePermanent, targetPerm, null, null);

        // Retain the upkeep copy ability per "except it has this ability"
        Card copiedCard = sourcePermanent.getCard();
        copiedCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());

        String targetName = targetPerm.getCard().getName();
        String logEntry = originalName + " becomes a copy of " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, targetName);

        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleCastFromLibraryChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (accepted) {
            List<Card> deck = gameData.playerDecks.get(player.getId());

            // Verify the card is still on top of the library
            if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToCast.getId())) {
                String logEntry = cardToCast.getName() + " is no longer on top of the library.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} no longer on top of library for cast-from-library", gameData.id, cardToCast.getName());
            } else {
                deck.removeFirst();

                List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                StackEntryType spellType = cardToCast.getType() == CardType.INSTANT
                        ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                if (cardToCast.isNeedsTarget()) {
                    // Targeted spell — need to choose target before putting on stack
                    List<UUID> validTargets = new ArrayList<>();
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (cardToCast.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                    validTargets.add(p.getId());
                                }
                            } else if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                    boolean canTargetPlayer = spellEffects.stream().anyMatch(CardEffect::canTargetPlayer);
                    if (canTargetPlayer) {
                        validTargets.addAll(gameData.orderedPlayerIds);
                    }

                    if (validTargets.isEmpty()) {
                        // No valid targets — spell can't be cast, put card back on top of library
                        deck.addFirst(cardToCast);
                        String logEntry = cardToCast.getName() + " has no valid targets.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} cast-from-library has no valid targets", gameData.id, cardToCast.getName());
                    } else {
                        gameData.interaction.setPermanentChoiceContext(
                                new PermanentChoiceContext.LibraryCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                        playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                "Choose a target for " + cardToCast.getName() + ".");

                        String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost — choosing target.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} casts {} from library, choosing target", gameData.id, playerName, cardToCast.getName());
                        return; // Wait for target choice
                    }
                } else {
                    // Non-targeted spell — put directly on stack
                    gameData.stack.add(new StackEntry(
                            spellType, cardToCast, player.getId(), cardToCast.getName(),
                            spellEffects, 0, (UUID) null, null
                    ));

                    gameData.spellsCastThisTurn.merge(player.getId(), 1, Integer::sum);
                    gameData.priorityPassedBy.clear();

                    String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToCast.getName());

                    triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                }
            }
        } else {
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to cast {} from library", gameData.id, playerName, cardToCast.getName());
        }

        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleMayNotUntapChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        // Find the permanent on the battlefield by Card identity
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

        if (accepted && sourcePermanent != null) {
            sourcePermanent.untap();
            String logEntry = player.getUsername() + " untaps " + sourceCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} untaps {} (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " chooses not to untap " + sourceCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} keeps {} tapped (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            // All may-not-untap choices resolved — complete the turn advance and resume auto-pass
            turnProgressionService.completeTurnAdvance(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleMaySacrificeArtifactForDividedDamage(GameData gameData, Player player,
                                                             boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            UUID controllerId = ability.controllerId();
            List<UUID> validArtifactIds = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (gameQueryService.isArtifact(p)) {
                        validArtifactIds.add(p.getId());
                    }
                }
            }

            if (validArtifactIds.isEmpty()) {
                String logEntry = player.getUsername() + " has no artifacts to sacrifice.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} has no artifacts to sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

                gameData.pendingETBDamageAssignments = Map.of();
                playerInputService.processNextMayAbility(gameData);
                if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                    gameData.priorityPassedBy.clear();
                    gameBroadcastService.broadcastGameState(gameData);
                    turnProgressionService.resolveAutoPass(gameData);
                }
                return;
            }

            Map<UUID, Integer> damageAssignments = gameData.pendingETBDamageAssignments;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.SacrificeArtifactForDividedDamage(
                            controllerId, ability.sourceCard(), damageAssignments));
            playerInputService.beginPermanentChoice(gameData, controllerId, validArtifactIds,
                    ability.sourceCard().getName() + " — Choose an artifact to sacrifice.");

            String logEntry = player.getUsername() + " accepts — choosing an artifact to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            gameData.pendingETBDamageAssignments = Map.of();

            String logEntry = player.getUsername() + " declines to sacrifice an artifact for " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
        }
    }
}



