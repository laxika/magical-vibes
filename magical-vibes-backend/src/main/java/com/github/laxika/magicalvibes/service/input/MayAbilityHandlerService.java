package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayAbilityHandlerService {

    private final MayCastHandlerService mayCastHandlerService;
    private final MayCopyHandlerService mayCopyHandlerService;
    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final MayMiscHandlerService mayMiscHandlerService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

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

        // Pending equipment attach — e.g. Auriok Survivors "you may attach it to this creature"
        UUID pendingEquipId = gameData.interaction.pendingEquipmentAttachEquipmentId();
        UUID pendingTargetId = gameData.interaction.pendingEquipmentAttachTargetId();
        if (pendingEquipId != null && pendingTargetId != null) {
            mayMiscHandlerService.handleEquipmentAttachChoice(gameData, player, accepted, pendingEquipId, pendingTargetId);
            return;
        }

        // Cast-from-library-without-paying — e.g. Galvanoth (second phase: cast prompt)
        CastTopOfLibraryWithoutPayingManaCostEffect castFromLibEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTopOfLibraryWithoutPayingManaCostEffect)
                .map(e -> (CastTopOfLibraryWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (castFromLibEffect != null && castFromLibEffect.castableTypes().contains(ability.sourceCard().getType())) {
            mayCastHandlerService.handleCastFromLibraryChoice(gameData, player, accepted, ability);
            return;
        }

        // Cast-from-graveyard — e.g. Chancellor of the Spires
        CastTargetInstantOrSorceryFromGraveyardEffect castFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElse(null);
        if (castFromGraveyardEffect != null) {
            mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability, castFromGraveyardEffect);
            return;
        }

        // May-not-untap choice from untap step (e.g. Rust Tick)
        boolean isMayNotUntap = ability.effects().stream().anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
        if (isMayNotUntap) {
            mayMiscHandlerService.handleMayNotUntapChoice(gameData, player, accepted, ability);
            return;
        }

        // Opening hand delayed counter trigger (e.g. Chancellor of the Annex)
        RegisterDelayedCounterTriggerEffect delayedCounterTrigger = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedCounterTriggerEffect)
                .map(e -> (RegisterDelayedCounterTriggerEffect) e)
                .findFirst().orElse(null);
        if (delayedCounterTrigger != null) {
            mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, delayedCounterTrigger);
            return;
        }

        // Opening hand delayed mana trigger (e.g. Chancellor of the Tangle)
        RegisterDelayedManaTriggerEffect delayedManaTrigger = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedManaTriggerEffect)
                .map(e -> (RegisterDelayedManaTriggerEffect) e)
                .findFirst().orElse(null);
        if (delayedManaTrigger != null) {
            mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, delayedManaTrigger);
            return;
        }

        // Counter-unless-pays — handled via the may ability system
        boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
        if (isCounterUnlessPays) {
            mayPenaltyChoiceHandlerService.handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Lose-life-unless-discard — handled via the may ability system
        boolean isLoseLifeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof LoseLifeUnlessDiscardEffect);
        if (isLoseLifeUnlessDiscard) {
            mayPenaltyChoiceHandlerService.handleLoseLifeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Opponent may return exiled card to hand, or controller draws N (e.g. Distant Memories)
        OpponentMayReturnExiledCardOrDrawEffect opponentExileChoice = ability.effects().stream()
                .filter(e -> e instanceof OpponentMayReturnExiledCardOrDrawEffect)
                .map(e -> (OpponentMayReturnExiledCardOrDrawEffect) e)
                .findFirst().orElse(null);
        if (opponentExileChoice != null) {
            mayPenaltyChoiceHandlerService.handleOpponentExileChoice(gameData, player, accepted, ability, opponentExileChoice);
            return;
        }

        // Sacrifice-unless-discard — handled via the may ability system
        boolean isSacrificeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect);
        if (isSacrificeUnlessDiscard) {
            mayPenaltyChoiceHandlerService.handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-return-own-permanent — handled via the may ability system
        boolean isSacrificeUnlessReturnPermanent = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessReturnOwnPermanentTypeToHandEffect);
        if (isSacrificeUnlessReturnPermanent) {
            mayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnOwnPermanentChoice(gameData, player, accepted, ability);
            return;
        }

        // Generic single-draw replacement
        ReplaceSingleDrawEffect replaceSingleDrawEffect = ability.effects().stream()
                .filter(e -> e instanceof ReplaceSingleDrawEffect)
                .map(e -> (ReplaceSingleDrawEffect) e)
                .findFirst()
                .orElse(null);
        if (replaceSingleDrawEffect != null) {
            mayMiscHandlerService.handleSingleDrawReplacementChoice(gameData, player, accepted, ability, replaceSingleDrawEffect);
            return;
        }

        // Copy spell retarget — choose new targets for a copied spell
        boolean isCopySpellRetarget = ability.effects().stream().anyMatch(e -> e instanceof CopySpellEffect);
        if (isCopySpellRetarget) {
            mayCopyHandlerService.handleCopySpellRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // BecomeCopyOfTargetCreatureEffect — targets "another creature" (e.g. Cryptoplasm)
        boolean isBecomeCopyEffect = ability.effects().stream().anyMatch(e -> e instanceof BecomeCopyOfTargetCreatureEffect);
        if (isBecomeCopyEffect) {
            mayCopyHandlerService.handleBecomeCopyChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy permanent effect (Clone / Sculpting Steel) — handled as replacement effect (pre-entry)
        CopyPermanentOnEnterEffect copyEffect = ability.effects().stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect != null) {
            mayCopyHandlerService.handleCopyPermanentOnEnterChoice(gameData, player, accepted, ability, copyEffect);
            return;
        }

        // Sacrifice-artifact for divided damage (e.g. Kuldotha Flamefiend)
        boolean isSacrificeArtifact = ability.effects().stream()
                .anyMatch(e -> e instanceof SacrificeArtifactThenDealDividedDamageEffect);
        if (isSacrificeArtifact) {
            mayMiscHandlerService.handleMaySacrificeArtifactForDividedDamage(gameData, player, accepted, ability);
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

        // Pre-targeted may ability — target was already chosen (e.g. "You may tap or untap that creature")
        if (accepted && isTargetedPermanentEffect && ability.targetCardId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, ability.targetCardId());
            if (target != null) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        0
                );
                entry.setTargetPermanentId(ability.targetCardId());
                gameData.stack.add(entry);

                String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName()
                        + "'s ability targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts pre-targeted may ability from {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = ability.sourceCard().getName() + "'s ability fizzles — target no longer exists.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pre-targeted may ability target gone", gameData.id, ability.sourceCard().getName());
            }

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
                turnProgressionService.resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted && isTargetedPermanentEffect) {
            handleTargetedMayAbilityAccepted(gameData, player, ability);
            return;
        }

        if (accepted) {
            StackEntry entry;
            if (ability.sourcePermanentId() != null) {
                // Combat damage trigger with source permanent and target context
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        ability.targetCardId(),
                        ability.sourcePermanentId()
                );
            } else {
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        xValuePaid
                );
            }

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

            // Effects that copy an entering permanent need the target permanent ID from the trigger
            boolean needsEnteringTarget = ability.effects().stream()
                    .anyMatch(e -> e instanceof CreateTokenCopyOfTargetPermanentEffect);
            if (needsEnteringTarget && ability.targetCardId() != null) {
                entry.setTargetPermanentId(ability.targetCardId());
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
}
