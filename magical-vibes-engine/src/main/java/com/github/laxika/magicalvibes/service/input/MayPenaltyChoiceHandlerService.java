package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCardFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessOwnerPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandDiscardMatchingCardsUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.WaterbendPaymentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayPenaltyChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibraryExileSupport libraryExileSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardTopExileSupport graveyardTopExileSupport;
    private final WaterbendPaymentService waterbendPaymentService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.RevealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler destroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReturnMatchingPermanentsUnlessOwnerPaysEffectHandler returnMatchingPermanentsUnlessOwnerPaysEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;
    private final LifeSupport lifeSupport;
    private final ResolutionCostPaymentService resolutionCostPaymentService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService coinFlipService;

    /**
     * Runs {@code payment}, the completion of {@code player} accepting {@code ability}, whose life payments
     * ({@code lifePayments}, each payer's total) change nothing before them but mana payments, which can't change what
     * the Lich's Duel Mastery sacrifice a payment causes can be replaced with (paying life is losing it, CR 119.4). That
     * sacrifice's CR 616.1 questions are asked before anything is paid, and the answer is completed again with them
     * once chosen; the window is then closed, so nothing after the questions happens twice.
     */
    private void payLifeAskingDeathReplacementsFirst(GameData gameData, Player player, PendingMayAbility ability,
                                                     Map<UUID, Integer> lifePayments, Runnable payment) {
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, true, () -> {
                    lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, lifePayments);
                    payment.run();
                });
    }

    public void handleLoseLifeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
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
                gameData.discardCausedByOpponent = ability.sourceControllerId() != null
                        && !ability.sourceControllerId().equals(targetPlayerId);
                playerInputService.beginDiscardChoice(gameData, targetPlayerId, validIndices,
                        "Choose a card to discard.", 1);

                String logEntry = player.getUsername() + " chooses to discard a card.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life, through LifeSupport like every other life loss.
        lifeSupport.applyLifeLoss(gameData, targetPlayerId, effect.lifeLoss(), ability.sourceCard().getName());
        log.info("Game {} - {} declined to discard ({})", gameData.id, player.getUsername(), ability.sourceCard().getName());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Lim-Dûl's Hex: each player independently may pay {@code manaCost} or take damage. Accepting
     * spends the mana; declining (or accepting without enough mana) damages that player. Either
     * way, the next remaining player is then offered the same choice (APNAP).
     */
    public void handleEachPlayerTakesDamageUnlessPaysChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability, EachPlayerTakesDamageUnlessPaysEffect effect) {
        UUID targetPlayerId = ability.controllerId();
        boolean paid = false;
        if (accepted) {
            // The prompt carries the effect's controller as its target (EachPlayerTakesDamageUnlessPaysEffectHandler).
            if (resolutionCostPaymentService.payManaFromPool(gameData, targetPlayerId,
                    new ManaCost(effect.manaCost()), ability.targetCardId())) {
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to avoid damage ({})", gameData.id, player.getUsername(),
                        effect.manaCost(), ability.sourceCard().getName());
            }
        }

        eachPlayerTakesDamageUnlessPaysEffectHandler.afterPlayerDecision(
                gameData, ability, effect, targetPlayerId, paid);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Sirocco: one pay-or-discard decision per matching revealed card. Accepting pays the life and
     * keeps that card; declining (or no longer being able to pay) discards it. Either way, the next
     * queued card is then offered.
     */
    public void handleRevealHandDiscardUnlessPaysLifeChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability,
            RevealHandDiscardMatchingCardsUnlessPaysLifeEffect effect) {
        UUID targetPlayerId = ability.controllerId();

        boolean canPay = resolutionCostPaymentService.canPayLife(gameData, targetPlayerId, effect.lifeCost());
        if (accepted && canPay) {
            payLifeAskingDeathReplacementsFirst(gameData, player, ability, Map.of(targetPlayerId, effect.lifeCost()),
                    () -> {
                        lifeSupport.applyLifePayment(gameData, targetPlayerId, effect.lifeCost(),
                                ability.sourceCard().getName());
                        revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler.afterCardDecision(
                                gameData, ability, effect, targetPlayerId, true);
                        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    });
            return;
        }

        revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler.afterCardDecision(
                gameData, ability, effect, targetPlayerId, false);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Giant Albatross: the damaging creature's controller either pays the life or that creature is
     * destroyed and can't be regenerated; either way the next queued damaging creature is offered, and
     * after the last decision the unpaid creatures are destroyed together (CR 101.4).
     */
    public void handleDestroyCreaturesThatDamagedSourceUnlessPaysLifeChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability,
            DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect) {
        UUID payingPlayerId = ability.controllerId(); // the damaging creature's controller — decision maker

        boolean canPay = gameQueryService.canPlayerLoseLife(gameData, payingPlayerId)
                && gameData.getLife(payingPlayerId) >= effect.lifeCost();
        if (accepted && canPay) {
            payLifeAskingDeathReplacementsFirst(gameData, player, ability, Map.of(payingPlayerId, effect.lifeCost()),
                    () -> {
                        destroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler.afterCreatureDecision(
                                gameData, ability, effect, true, () -> {
                                    lifeSupport.applyLifePayment(gameData, payingPlayerId, effect.lifeCost(),
                                            ability.sourceCard().getName());
                                    log.info("Game {} - {} pays {} life to save their creature ({})", gameData.id,
                                            player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
                                });
                        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    });
            return;
        }

        destroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler.afterCreatureDecision(
                gameData, ability, effect, false, () -> { });
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Cut the Tethers: one pay-or-be-bounced decision per matching permanent, made by that
     * permanent's OWNER ("unless that player pays" refers back to the owner just named, so a
     * stolen permanent is paid for by the player whose hand it would return to). Accepting spends
     * the mana and keeps it; declining (or accepting without the mana) returns it to its owner's
     * hand. Either way the next queued permanent is then offered.
     */
    public void handleReturnMatchingPermanentsUnlessOwnerPaysChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability,
            ReturnMatchingPermanentsUnlessOwnerPaysEffect effect) {
        UUID payingPlayerId = ability.controllerId();

        boolean paid = false;
        if (accepted) {
            if (resolutionCostPaymentService.payManaFromPool(gameData, payingPlayerId,
                    new ManaCost(effect.manaCost()), ability.sourceControllerId())) {
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to keep a permanent ({})", gameData.id,
                        player.getUsername(), effect.manaCost(), ability.sourceCard().getName());
            }
        }

        returnMatchingPermanentsUnlessOwnerPaysEffectHandler.afterPermanentDecision(
                gameData, ability, effect, paid);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Asks {@code opponentId} whether to let {@code effect}'s controller put {@code exiledCard} into their hand ("any
     * opponent may", Distant Memories).
     */
    public static void offerOpponentExileChoice(GameData gameData, Card exiledCard, UUID opponentId,
                                                OpponentMayReturnExiledCardOrDrawEffect effect) {
        String controllerName = gameData.playerIdToName.get(effect.controllerId());
        String prompt = "Let " + controllerName + " put the exiled card into their hand? If no opponent does, they draw "
                + effect.drawCount() + " cards.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                exiledCard, opponentId, List.of(effect), prompt, exiledCard.getId()));
    }

    public void handleOpponentExileChoice(GameData gameData, Player player, boolean accepted,
                                           PendingMayAbility ability, OpponentMayReturnExiledCardOrDrawEffect effect) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID exiledCardId = ability.targetCardId();
        UUID controllerId = effect.controllerId();

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (!accepted) {
            // "Any opponent may": the next opponent in turn order decides; the controller draws only if none does
            // (Distant Memories ruling 2011-06-01).
            List<UUID> opponents = com.github.laxika.magicalvibes.service.effect.SubjectResolutionService
                    .apnapOpponents(gameData, controllerId);
            int next = opponents.indexOf(opponentId) + 1;
            ExiledCardEntry stillExiled = gameData.findExiledCard(exiledCardId);
            if (next > 0 && next < opponents.size() && stillExiled != null) {
                gameLogService.append(gameData, GameLog.text(opponentName + " declines."));
                offerOpponentExileChoice(gameData, stillExiled.card(), opponents.get(next), effect);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        if (accepted) {
            // Opponent lets the controller have the exiled card — move from exile to hand
            Card exiledCard = null;
            ExiledCardEntry exileEntry = gameData.findExiledCard(exiledCardId);
            if (exileEntry != null) {
                exiledCard = exileEntry.card();
                gameData.removeFromExile(exiledCardId);
            }

            if (exiledCard != null) {
                gameData.addCardToHand(controllerId, exiledCard);
                gameLogService.append(gameData, GameLog.textCardText(
                        opponentName + " allows it. " + controllerName + " puts ", exiledCard, " into their hand."));
                log.info("Game {} - {} allows exile return, {} gets {}", gameData.id, opponentName, controllerName, exiledCard.getName());
            }
        } else {
            // Opponent declines — controller draws cards
            int drawCount = effect.drawCount();
            for (int i = 0; i < drawCount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }

            String logEntry = opponentName + " declines. " + controllerName + " draws " + drawCount + " cards.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines exile return, {} draws {}", gameData.id, opponentName, controllerName, drawCount);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * An accepted cost that pays life (a life cost, or a mana cost with life or Phyrexian mana in it) asks the Lich's
     * Duel Mastery sacrifice the payment would cause before anything of the answer happens
     * ({@link #payLifeAskingDeathReplacementsFirst}).
     */
    public void handleForcedCostOrElseOptionalChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ForcedCostOrElseEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ForcedCostOrElseEffect)
                .map(e -> (ForcedCostOrElseEffect) e)
                .findFirst().orElseThrow();
        Map<UUID, Integer> lifePayments = accepted ? acceptedForcedCostLifePayments(gameData, ability, effect) : Map.of();
        if (!lifePayments.isEmpty()) {
            payLifeAskingDeathReplacementsFirst(gameData, player, ability, lifePayments,
                    () -> completeForcedCostOrElseOptionalChoice(gameData, player, true, ability, effect));
            return;
        }
        completeForcedCostOrElseOptionalChoice(gameData, player, accepted, ability, effect);
    }

    /** The life an accepted forced cost will make its payer pay, or an empty map when it pays none. */
    private Map<UUID, Integer> acceptedForcedCostLifePayments(GameData gameData, PendingMayAbility ability,
                                                              ForcedCostOrElseEffect effect) {
        UUID payerId = ability.controllerId();
        int lifeAmount = 0;
        if (effect.forcedCost() instanceof PayLifeCost payLifeCost) {
            lifeAmount = effectiveLifeAmount(gameData, payerId, ability, payLifeCost);
            if (!resolutionCostPaymentService.canPayLife(gameData, payerId, lifeAmount)) {
                lifeAmount = 0;
            }
        } else if (effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayManaCost payCost
                && canPayManaCost(gameData, payerId, ability, payCost)) {
            ManaCost cost = new ManaCost(ability.manaCost(), payCost.forCumulativeUpkeep());
            lifeAmount = payCost.lifeAmount() + (cost.hasPhyrexianMana()
                    ? cost.payPhyrexianManaAuto(new ManaPool(gameData.playerManaPools.get(payerId)), 0)
                    : 0);
        }
        return lifeAmount > 0 ? Map.of(payerId, lifeAmount) : Map.of();
    }

    private void completeForcedCostOrElseOptionalChoice(GameData gameData, Player player, boolean accepted,
                                                        PendingMayAbility ability, ForcedCostOrElseEffect effect) {

        UUID decidingPlayerId = ability.controllerId();
        UUID sourceControllerId = forcedCostSourceControllerId(gameData, ability, effect);

        if (effect.villainousChoice()) {
            var villainousState = gameData.villainousChoice;
            if (villainousState.additionalChoicesRemaining > 0) {
                villainousState.additionalChoicesRemaining--;
                gameData.pendingMayAbilities.addFirst(ability);
            } else {
                villainousState.additionalChoicesRemaining = -1;
            }
        }

        if (accepted && effect.forcedCost() instanceof PayLifeCost payLifeCost) {
            int lifeAmount = effectiveLifeAmount(gameData, decidingPlayerId, ability, payLifeCost);
            boolean canPay = resolutionCostPaymentService.canPayLife(gameData, decidingPlayerId, lifeAmount);
            if (canPay) {
                lifeSupport.applyLifePayment(gameData, decidingPlayerId, lifeAmount,
                        ability.sourceCard().getName());
                log.info("Game {} - {} pays {} life to avoid penalty ({})", gameData.id,
                        player.getUsername(), lifeAmount, ability.sourceCard().getName());
                forcedCostOrElseEffectHandler.resolvePaidEffects(gameData, ability, effect, 0);
                clearAnyPlayerPayState(gameData);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (effect.anyPlayerMayPay() && offerNextAnyPlayerPay(gameData, ability, effect)) {
                return;
            }
        }

        if (accepted && effect.forcedCost() instanceof PayEnergyCost energyCost) {
            int energy = gameData.playerEnergyCounters.getOrDefault(decidingPlayerId, 0);
            if (energy >= energyCost.fixedAmount()) {
                gameData.setPlayerEnergyCounters(decidingPlayerId, energy - energyCost.fixedAmount());
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + energyCost.fixedAmount()
                                + " energy counter(s). (", ability.sourceCard(), ")"));
                forcedCostOrElseEffectHandler.resolvePaidEffects(gameData, ability, effect, 0);
                clearAnyPlayerPayState(gameData);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (effect.anyPlayerMayPay() && offerNextAnyPlayerPay(gameData, ability, effect)) {
                return;
            }
        }

        if (accepted && effect.forcedCost() instanceof DiscardCardTypeCost discardCost) {
            List<Integer> validIndices = matchingHandIndices(gameData, decidingPlayerId, discardCost);
            if (validIndices.size() >= discardCost.count()) {
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(gameData, decidingPlayerId, validIndices,
                        "Choose a card to discard for cumulative upkeep.", discardCost.count());
                return;
            }
            // The hand changed since the may prompt; fall through to the unpaid branch.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.FlipCoinsCost flipCost) {
            // Flipping is the first thing the payment changes, so a Krark's Thumb choice on any of its flips can
            // stop it and complete this answer again once the kept results are chosen.
            coinFlipService.answerChoosingResultsFirst(gameData, () -> {
                forcedCostOrElseEffectHandler.payFlipCoins(
                        gameData, decidingPlayerId, ability.sourceCard(), flipCost.count());
                clearAnyPlayerPayState(gameData);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }, new GameData.MayAnswerReplay(player, ability, true));
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.WaterbendCost waterbendCost
                && waterbendPaymentService.canPay(gameData, decidingPlayerId, waterbendCost.amount())) {
            int amount = waterbendCost.amount();
            List<UUID> choosable = waterbendPaymentService.untappedArtifactsAndCreatures(gameData, decidingPlayerId);
            int minimumTaps = waterbendPaymentService.minimumTaps(gameData, decidingPlayerId, amount);
            if (choosable.size() > minimumTaps) {
                // The payer decides which permanents to tap (CR 701.67a); the cost is paid once they choose.
                playerInputService.beginMultiPermanentChoice(gameData, decidingPlayerId, choosable,
                        Math.min(amount, choosable.size()),
                        new MultiPermanentChoiceContext.ForcedCostWaterbendPayment(
                                ability, effect, amount, minimumTaps),
                        ability.sourceCard().getName() + " — Choose artifacts and creatures to tap for waterbend {"
                                + amount + "}; the rest is paid with mana.");
                return;
            }
            waterbendPaymentService.pay(gameData, decidingPlayerId, amount, ability.sourceCard());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays {" + amount + "} using waterbend. (", ability.sourceCard(), ")"));
            forcedCostOrElseEffectHandler.resolvePaidEffects(gameData, ability, effect, 0);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayManaCost payCost) {
            // Use the cost stored on the pending ability — it already reflects any dynamic
            // reduction (Draco's Domain) resolved when the prompt was created.
            String costString = ability.manaCost();
            ManaCost cost = new ManaCost(costString, payCost.forCumulativeUpkeep());
            ManaPool pool = gameData.playerManaPools.get(decidingPlayerId);
            int lifeAmount = payCost.lifeAmount();
            int phyrexianLifeAmount = cost.hasPhyrexianMana()
                    ? cost.payPhyrexianManaAuto(new ManaPool(pool), 0)
                    : 0;
            if (canPayManaCost(gameData, decidingPlayerId, ability, payCost)) {
                var manaBefore = pool.getAllManaTotals();
                int totalBefore = pool.getTotalAllMana();
                if (cost.hasPhyrexianMana()) {
                    cost.payPhyrexianManaAuto(pool, 0);
                }
                cost.payResolutionCost(pool);
                // Mana paid for another player's permanent or spell is a tax paid to them, as the activation and
                // cast taxes are.
                ActionEvents.taxPaid(gameData, decidingPlayerId, sourceControllerId,
                        totalBefore - pool.getTotalAllMana());
                var manaSpent = ManaPool.coloredManaSpent(manaBefore, pool.getAllManaTotals(), null);
                int blackOrRedSpent = manaSpent.getOrDefault(ManaColor.BLACK, 0)
                        + manaSpent.getOrDefault(ManaColor.RED, 0);
                forcedCostOrElseEffectHandler.resolvePaidEffects(gameData, ability, effect, blackOrRedSpent);
                if (lifeAmount > 0) {
                    lifeSupport.applyLifePayment(gameData, decidingPlayerId, lifeAmount,
                            ability.sourceCard().getName());
                    // A blank mana cost means the payment is life-only (Glacial Chasm).
                    String paidText = costString == null || costString.isEmpty()
                            ? lifeAmount + " life"
                            : costString + " and " + lifeAmount + " life";
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " pays " + paidText + ". (", ability.sourceCard(), ")"));
                    log.info("Game {} - {} pays {} and {} life to avoid penalty ({})",
                            gameData.id, player.getUsername(), costString, lifeAmount,
                            ability.sourceCard().getName());
                } else {
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " pays " + costString + ". (", ability.sourceCard(), ")"));
                    log.info("Game {} - {} pays {} to avoid penalty ({})", gameData.id, player.getUsername(),
                            costString, ability.sourceCard().getName());
                }
                if (phyrexianLifeAmount > 0) {
                    lifeSupport.applyLifePayment(gameData, decidingPlayerId, phyrexianLifeAmount,
                            "Phyrexian mana");
                }
                clearAnyPlayerPayState(gameData);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — for any-player, offer the next player; otherwise
            // fall through to the penalty.
            if (effect.anyPlayerMayPay() && offerNextAnyPlayerPay(gameData, ability, effect)) {
                return;
            }
        }

        if (accepted && effect.forcedCost() instanceof OpponentGainsLifeCost lifeCost
                && forcedCostOrElseEffectHandler.payOpponentGainsLifeCost(
                gameData, sourceControllerId, lifeCost, ability.sourceCard(), null)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && forcedCostOrElseEffectHandler.beginCumulativeUpkeepGraveyardPayment(
                gameData, ability, sourceControllerId)) {
            return;
        }

        if (accepted && effect.forcedCost() instanceof PutCardsFromGraveyardOnBottomOfLibraryCost
                && forcedCostOrElseEffectHandler.beginControllerGraveyardPayment(
                gameData, ability, sourceControllerId)) {
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost exileCost) {
            if (libraryExileSupport.hasAtLeast(gameData, sourceControllerId, exileCost.count())) {
                libraryExileSupport.exileTopCards(gameData, sourceControllerId, exileCost.count());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but the library ran short — fall through to the penalty.
        }

        if (accepted) {
            var millPayment = forcedCostOrElseEffectHandler.tryPayMillControllerCost(
                    gameData, effect, sourceControllerId);
            if (millPayment.orElse(false)) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost graveyardCost) {
            if (graveyardTopExileSupport.exileTopMatching(gameData, sourceControllerId, graveyardCost.requiredType())) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but the graveyard no longer holds a matching card — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost graveyardCost) {
            List<Integer> matchingIndices = graveyardTopExileSupport.matchingIndices(
                    gameData, sourceControllerId, graveyardCost);
            if (matchingIndices.size() == 1
                    && graveyardTopExileSupport.exileSoleMatching(gameData, sourceControllerId, graveyardCost)) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (matchingIndices.size() > 1) {
                gameData.pendingEffectResolutionEntry = null;
                gameData.pendingEffectResolutionIndex = 0;
                interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                        .builder(sourceControllerId, matchingIndices, GraveyardChoiceDestination.EXILE,
                                "Choose a card to exile from your graveyard.")
                        .mandatory(true)
                        .build());
                return;
            }
            // Accepted but the graveyard no longer holds a matching card — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost tokenCost) {
            // Nothing can make this cost unpayable, so accepting always pays it in full.
            UUID opponentId = gameQueryService.getOpponentId(gameData, sourceControllerId);
            for (int i = 0; opponentId != null && i < tokenCost.count(); i++) {
                destructionSupport.createTokenForPlayer(gameData, opponentId, tokenCost.tokenTemplate(),
                        ability.sourceCard().getName(), ability.sourceCard().getSetCode());
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.DrawCardsCost drawCost) {
            // Nothing can make this cost unpayable, so accepting always pays it in full.
            for (int i = 0; i < drawCost.count(); i++) {
                drawService.resolveDrawCard(gameData, sourceControllerId);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost counterCost) {
            StackEntry counterEntry = StackEntry.carrier(ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", List.of(effect),
                    ability.targetCardId(), ability.sourcePermanentId());
            counterEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            counterEntry.setActivePlayerId(ability.activePlayerId());
            if (forcedCostOrElseEffectHandler.payCounterOnSourceCost(
                    gameData, counterEntry, counterCost, decidingPlayerId)) {
                forcedCostOrElseEffectHandler.resolvePaidEffects(gameData, counterEntry, effect, 0);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost counterCost) {
            StackEntry counterEntry = StackEntry.carrier(ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", List.of(effect),
                    ability.targetCardId(), ability.sourcePermanentId());
            if (forcedCostOrElseEffectHandler.payCounterFromSourceCost(gameData, counterEntry, counterCost)) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but the source or required counters are gone — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof PutCounterOnControlledCreatureCost counterCost) {
            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, decidingPlayerId,
                    permanent -> !gameQueryService.cantHaveCounters(gameData, permanent)
                            && (counterCost.counterType() != CounterType.MINUS_ONE_MINUS_ONE
                            || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                            && (counterCost.counterType() != CounterType.PLUS_ONE_PLUS_ONE
                            || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent))
                            && !gameQueryService.cantBeChosen(gameData, permanent));
            if (!candidates.isEmpty()) {
                StackEntry counterEntry = StackEntry.carrier(ability.sourceCard(), sourceControllerId,
                        ability.sourceCard().getName() + "'s ability", List.of(effect),
                        ability.targetCardId(), ability.sourcePermanentId());
                counterEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
                if (forcedCostOrElseEffectHandler.putCounterOnChosenPermanent(
                        gameData, decidingPlayerId, candidates, counterEntry, counterCost)) {
                    if (candidates.size() == 1) {
                        clearAnyPlayerPayState(gameData);
                        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    }
                    return;
                }
            }
            // Accepted but no eligible creature remains — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof PutCounterOnOpponentCreatureCost counterCost) {
            UUID opponentId = gameQueryService.getOpponentId(gameData, decidingPlayerId);
            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, opponentId,
                    permanent -> !gameQueryService.cantHaveCounters(gameData, permanent)
                            && (counterCost.counterType() != CounterType.MINUS_ONE_MINUS_ONE
                            || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                            && (counterCost.counterType() != CounterType.PLUS_ONE_PLUS_ONE
                            || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)));
            if (!candidates.isEmpty()) {
                StackEntry counterEntry = StackEntry.carrier(ability.sourceCard(), sourceControllerId,
                        ability.sourceCard().getName() + "'s ability", List.of(effect),
                        ability.targetCardId(), ability.sourcePermanentId());
                counterEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
                if (forcedCostOrElseEffectHandler.putCounterOnChosenPermanent(
                        gameData, decidingPlayerId, candidates, counterEntry, counterCost)) {
                    if (candidates.size() == 1) {
                        clearAnyPlayerPayState(gameData);
                        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    }
                    return;
                }
            }
        }

        if (accepted && effect.forcedCost() instanceof GainControlOfPermanentsCost gainCost) {
            UUID payerId = effect.payerIsEnchantedController() || effect.payerIsDefendingPlayer()
                    ? decidingPlayerId : sourceControllerId;
            List<UUID> candidates = destructionSupport.collectPermanentIdsNotControlledBy(
                    gameData, payerId, gainCost.filter());
            if (candidates.size() >= gainCost.count()
                    && forcedCostOrElseEffectHandler.beginGainControlPayment(
                    gameData, payerId, candidates, ability.sourceCard(), ability.sourcePermanentId(),
                    effect, gainCost.count())) {
                if (candidates.size() == 1) {
                    clearAnyPlayerPayState(gameData);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
            // Accepted but no eligible permanent remains — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost) {
            if (forcedCostOrElseEffectHandler.beginControlledCounterPayment(
                    gameData, sourceControllerId, ability, effect)) {
                if (!gameData.interaction.isAwaitingInput()) {
                    clearAnyPlayerPayState(gameData);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
            // Accepted but every counter is gone — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost multiCost) {
            List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, sourceControllerId,
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, multiCost.filter()));
            if (matchingIds.size() >= multiCost.count()) {
                // sacrificePlayerMatchingPermanents sacrifices all when the count matches exactly, or
                // begins a multi-select choice when the controller has more than needed. The choice's
                // completion continues the game itself, so only auto-pass here when nothing is pending.
                destructionSupport.sacrificePlayerMatchingPermanents(gameData, sourceControllerId, multiCost.count(), multiCost.filter());
                if (matchingIds.size() == multiCost.count()) {
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
            // Accepted but no longer enough to sacrifice — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost returnCost) {
            List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, sourceControllerId,
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, returnCost.filter()));
            if (matchingIds.size() >= returnCost.count()) {
                destructionSupport.returnPlayerMatchingPermanents(gameData, sourceControllerId, returnCost.count(), returnCost.filter());
                if (matchingIds.size() == returnCost.count()) {
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
            // Accepted but no longer enough to return — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost returnFromGraveyardCost) {
            if (forcedCostOrElseEffectHandler.hasMatchingGraveyardCard(gameData, sourceControllerId,
                    returnFromGraveyardCost.predicate())) {
                // The graveyard-choice completion continues the game itself (resolveAutoPass), and
                // this effect is the last one on its stack entry, so clear the paused resolution state.
                gameData.pendingEffectResolutionEntry = null;
                gameData.pendingEffectResolutionIndex = 0;
                clearAnyPlayerPayState(gameData);
                forcedCostOrElseEffectHandler.beginGraveyardReturnToHandChoice(gameData, sourceControllerId,
                        returnFromGraveyardCost.predicate());
                return;
            }
            // Accepted but the graveyard no longer holds a matching card — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof PutCardFromGraveyardOnBottomOfLibraryCost) {
            List<Card> graveyard = gameData.playerGraveyards.get(sourceControllerId);
            if (graveyard != null && !graveyard.isEmpty()) {
                gameData.pendingEffectResolutionEntry = null;
                gameData.pendingEffectResolutionIndex = 0;
                clearAnyPlayerPayState(gameData);
                forcedCostOrElseEffectHandler.beginGraveyardBottomChoice(gameData, sourceControllerId);
                return;
            }
            // Accepted but the graveyard is empty — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof SacrificePermanentCost sacrificeCost) {
            UUID sacrificingPlayerId = sacrificingPlayerId(effect, decidingPlayerId, sourceControllerId);
            List<UUID> matchingIds = sacrificeCostCandidates(gameData, ability, sacrificeCost,
                    sacrificingPlayerId, sourceControllerId);

            if (matchingIds.size() == 1) {
                Permanent perm = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
                if (perm != null) {
                    destructionSupport.sacrificeAndLog(gameData, perm, sacrificingPlayerId);
                    clearAnyPlayerPayState(gameData);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
            } else if (matchingIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ForcedCostOrElse(sacrificingPlayerId, ability.sourcePermanentId(),
                                ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, matchingIds,
                        "Choose a permanent to sacrifice (" + sacrificeCost.description() + ").");
                return;
            }
            if (effect.villainousChoice()) {
                // CR 701.55b: the option is performed as far as possible, which here is not at all.
                clearAnyPlayerPayState(gameData);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but nothing left to sacrifice — fall through to the penalty.
        }

        // Declined (or unable to pay) — for any-player, offer the next player before the penalty.
        if (effect.anyPlayerMayPay() && offerNextAnyPlayerPay(gameData, ability, effect)) {
            return;
        }

        // Declined (or unable to pay) — resolve the fallback/penalty effects.
        // Preserve targetCardId so "that player" damage (DestructionSupport reads it) / similar fallthroughs
        // still see the enchanted permanent's controller (Mind Whip).
        clearAnyPlayerPayState(gameData);
        StackEntry syntheticEntry = StackEntry.carrier(ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName() + "'s ability", List.of(effect),
                ability.targetCardId(), ability.sourcePermanentId());
        syntheticEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        syntheticEntry.setAttackedTargetId(ability.attackedTargetId());
        if (effect.payerIsEnchantedController()) {
            syntheticEntry.setTriggeringPermanentControllerId(ability.controllerId());
            // "That player sacrifices it": the permanent the Aura enchanted as the ability triggered, which the
            // source snapshot taken then records.
            Permanent auraAtTrigger = ability.sourcePermanentSnapshot();
            Permanent enchantedAtTrigger = auraAtTrigger == null || auraAtTrigger.getAttachedTo() == null ? null
                    : gameQueryService.findPermanentById(gameData, auraAtTrigger.getAttachedTo());
            if (enchantedAtTrigger != null) {
                syntheticEntry.setAttachedPermanentSnapshot(new Permanent(enchantedAtTrigger));
            }
        }
        destructionSupport.resolveForcedCostElseEffects(gameData, syntheticEntry, effect);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Whether accepting {@code ability}, a non-villainous {@link ForcedCostOrElseEffect} prompt, goes straight to
     * sacrificing what its sacrifice cost names without changing anything first: the payer controls exactly one
     * permanent a {@link SacrificePermanentCost} can take, or exactly as many as a
     * {@link com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost} needs. The cost-specific
     * branches of {@link #handleForcedCostOrElseOptionalChoice} that run before those sacrifices are guarded by
     * their own cost types, so they only read.
     */
    public boolean acceptedCostSacrificesWithoutChoice(GameData gameData, PendingMayAbility ability,
                                                       ForcedCostOrElseEffect effect) {
        UUID sourceControllerId = forcedCostSourceControllerId(gameData, ability, effect);
        if (effect.forcedCost() instanceof SacrificePermanentCost sacrificeCost) {
            UUID sacrificingPlayerId = sacrificingPlayerId(effect, ability.controllerId(), sourceControllerId);
            return sacrificeCostCandidates(gameData, ability, sacrificeCost, sacrificingPlayerId, sourceControllerId)
                    .size() == 1;
        }
        if (effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost multiCost) {
            return destructionSupport.collectPermanentIds(gameData, sourceControllerId,
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, multiCost.filter()))
                    .size() == multiCost.count();
        }
        return false;
    }

    /**
     * The controller of {@code ability}'s source. For a normal {@link ForcedCostOrElseEffect} it is the deciding
     * player; for an "any player may pay", enchanted-controller or defending-player prompt the deciding player is
     * the payer being asked and {@link GameData#forcedCostOrElseSourceControllerId} holds the source controller.
     */
    private static UUID forcedCostSourceControllerId(GameData gameData, PendingMayAbility ability,
                                                     ForcedCostOrElseEffect effect) {
        if (ability.sourceControllerId() != null) {
            return ability.sourceControllerId();
        }
        boolean payerIsNotSourceController = effect.anyPlayerMayPay() || effect.payerIsEnchantedController()
                || effect.payerIsDefendingPlayer();
        return payerIsNotSourceController && gameData.forcedCostOrElseSourceControllerId != null
                ? gameData.forcedCostOrElseSourceControllerId
                : ability.controllerId();
    }

    /**
     * Who sacrifices for a {@link SacrificePermanentCost}: the deciding player when they are the stack-target
     * payer (Pillar Tombs of Aku / Mind Whip-shaped each-upkeep sacrifice), otherwise the source controller.
     */
    private static UUID sacrificingPlayerId(ForcedCostOrElseEffect effect, UUID decidingPlayerId,
                                            UUID sourceControllerId) {
        return effect.payerIsEnchantedController() || effect.payerIsDefendingPlayer()
                ? decidingPlayerId : sourceControllerId;
    }

    private List<UUID> sacrificeCostCandidates(GameData gameData, PendingMayAbility ability,
                                               SacrificePermanentCost sacrificeCost, UUID sacrificingPlayerId,
                                               UUID sourceControllerId) {
        UUID sourcePermanentId = ability.sourcePermanentId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(ability.sourceCard().getId())
                .withSourceControllerId(sourceControllerId)
                .withSourcePermanentSnapshot(ability.sourcePermanentSnapshot())
                .withSourcePermanentId(sourcePermanentId);
        return destructionSupport.collectPermanentIds(gameData, sacrificingPlayerId,
                p -> (!sacrificeCost.excludeSource() || !p.getId().equals(sourcePermanentId))
                        && predicateEvaluationService.matchesPermanentPredicate(p, sacrificeCost.filter(),
                        filterContext));
    }

    /**
     * Whether {@code player} accepting {@code ability}, a non-villainous {@link ForcedCostOrElseEffect} prompt
     * that only its controller may pay, falls through to the fallback effects without changing anything first:
     * a mana cost the pool can't pay (Phantasmal Forces accepted without blue mana). The cost-specific branches
     * of {@link #handleForcedCostOrElseOptionalChoice} that run before such a fall-through only read.
     */
    public boolean acceptedCostFallsThroughUnpaid(GameData gameData, PendingMayAbility ability,
                                                  ForcedCostOrElseEffect effect) {
        return effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayManaCost payCost
                && !canPayManaCost(gameData, ability.controllerId(), ability, payCost);
    }

    /**
     * Whether {@code payerId} can pay the mana cost stored on {@code ability} (it already reflects any dynamic
     * reduction resolved when the prompt was created) from their mana pool, plus {@code payCost}'s life and any
     * Phyrexian mana paid with life.
     */
    private boolean canPayManaCost(GameData gameData, UUID payerId, PendingMayAbility ability,
                                   com.github.laxika.magicalvibes.model.effect.PayManaCost payCost) {
        ManaCost cost = new ManaCost(ability.manaCost(), payCost.forCumulativeUpkeep());
        ManaPool simulatedPool = new ManaPool(gameData.playerManaPools.get(payerId));
        int phyrexianLifeAmount = cost.hasPhyrexianMana()
                ? cost.payPhyrexianManaAuto(simulatedPool, 0)
                : 0;
        int totalLifeAmount = payCost.lifeAmount() + phyrexianLifeAmount;
        boolean canPayLife = resolutionCostPaymentService.canPayLife(gameData, payerId, totalLifeAmount);
        return cost.canPayResolutionCost(simulatedPool) && canPayLife;
    }

    private int effectiveLifeAmount(GameData gameData, UUID payerId, PendingMayAbility ability,
            PayLifeCost cost) {
        return forcedCostOrElseEffectHandler.forcedLifeCostAmount(gameData, payerId, ability.sourcePermanentId(), cost);
    }

    private List<Integer> matchingHandIndices(GameData gameData, UUID playerId,
            DiscardCardTypeCost cost) {
        List<Integer> matchingIndices = new ArrayList<>();
        List<Card> hand = gameData.playerHands.get(playerId);
        for (int i = 0; hand != null && i < hand.size(); i++) {
            Card card = hand.get(i);
            if (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null)) {
                matchingIndices.add(i);
            }
        }
        return matchingIndices;
    }

    /** Queues the next APNAP player for an anyPlayerMayPay ForcedCostOrElse, or returns false if none remain. */
    private boolean offerNextAnyPlayerPay(GameData gameData, PendingMayAbility ability, ForcedCostOrElseEffect effect) {
        if (gameData.forcedCostOrElseRemainingPlayers.isEmpty()) {
            return false;
        }
        UUID next = gameData.forcedCostOrElseRemainingPlayers.removeFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                ability.sourceCard(), next, List.of(effect), ability.description(),
                null, ability.manaCost(), ability.sourcePermanentId()));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        return true;
    }

    private void clearAnyPlayerPayState(GameData gameData) {
        gameData.forcedCostOrElseRemainingPlayers.clear();
        gameData.forcedCostOrElseSourceControllerId = null;
    }
}
