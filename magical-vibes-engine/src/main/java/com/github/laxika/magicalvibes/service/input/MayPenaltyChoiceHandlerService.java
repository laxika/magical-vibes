package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameStatus;
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
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombustibleGearhulkEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessCollectsEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessExilesGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessReturnLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUnlessDiscardCardTypeEffect;
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
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DrawCardUnlessPaysEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CounterSupport;
import com.github.laxika.magicalvibes.service.effect.WaterbendPaymentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayPenaltyChoiceHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final StateTriggerService stateTriggerService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LibraryExileSupport libraryExileSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardTopExileSupport graveyardTopExileSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DiscardHandUnlessPaysLifeEffectHandler discardHandUnlessPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.StealDyingOpponentPermanentUnlessPaysLifeEffectHandler stealDyingOpponentPermanentUnlessPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.TapTargetCreatureUnlessControllerPaysLifeEffectHandler tapTargetCreatureUnlessControllerPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffectHandler destroyTargetCreatureUnlessControllerPaysToughnessLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTargetCreatureUnlessControllerPaysEffectHandler returnTargetCreatureUnlessControllerPaysEffectHandler;
    private final CounterSupport counterSupport;
    private final WaterbendPaymentService waterbendPaymentService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.RevealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler destroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageControllerUnlessDiscardThenTapSourceEffectHandler damageControllerUnlessDiscardThenTapSourceEffectHandler;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.MustAttackUnlessControllerPaysManaValueEffectHandler mustAttackUnlessControllerPaysManaValueEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReturnMatchingPermanentsUnlessOwnerPaysEffectHandler returnMatchingPermanentsUnlessOwnerPaysEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;
    private final LifeSupport lifeSupport;

    /**
     * Arcum's Whistle: the active player may pay {X} (X = the target creature's mana value).
     * Paying spends the mana and nothing else happens; declining — or accepting without enough
     * mana — makes the creature attack this turn if able and schedules the conditional end-step
     * destruction.
     */
    public void handleMustAttackUnlessControllerPaysManaValueChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability) {
        UUID payingPlayerId = ability.controllerId();
        UUID targetPermanentId = ability.targetCardId();

        if (accepted) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payingPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + ability.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to avoid the must-attack penalty ({})", gameData.id,
                        player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — fall through to the penalty.
        }

        UUID abilityControllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        if (abilityControllerId == null) {
            abilityControllerId = payingPlayerId;
        }
        mustAttackUnlessControllerPaysManaValueEffectHandler.applyPenalty(
                gameData, ability.sourceCard(), abilityControllerId, targetPermanentId);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CounterUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessEffect ce && ce.ransomKind() == CounterUnlessEffect.RansomKind.PAY_MANA)
                .map(e -> (CounterUnlessPaysEffect) e)
                .findFirst().orElseThrow();
        int amount = effect.amount();
        int lifeCost = effect.lifeCost();
        boolean exileIfCountered = effect.exileIfCountered();
        List<CardEffect> onNotPaidEffects = effect.onNotPaidEffects();
        List<CardEffect> onPaidEffects = effect.onPaidEffects();
        UUID targetCardId = ability.targetCardId();
        String manaCost = effect.manaCost() != null ? effect.manaCost() : "{" + amount + "}";
        String costText = manaCost.equals("{0}") && lifeCost > 0
                ? lifeCost + " life"
                : manaCost + (lifeCost > 0 ? " and " + lifeCost + " life" : "");

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isProtectedFromCounterBySourceCard(gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    ability.sourceCard().getColor().name().toLowerCase());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            ManaCost cost = new ManaCost(manaCost);
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            boolean canPayLife = lifeCost <= 0
                    || (gameQueryService.canPlayerLifeChange(gameData, player.getId())
                            && gameData.getLife(player.getId()) >= lifeCost);
            if (cost.canPay(pool) && canPayLife) {
                cost.pay(pool);
                if (lifeCost > 0) {
                    lifeSupport.applyLifePayment(gameData, player.getId(), lifeCost,
                            ability.sourceCard().getName());
                }
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + costText + ". ", targetEntry.getCard(), " is not countered."));
                log.info("Game {} - {} pays {} to avoid counter", gameData.id, player.getUsername(), costText);
                counterSupport.resolvePaidRider(gameData, ability.sourceCard(), ability.sourceControllerId(),
                        onPaidEffects);
            } else {
                counterSpell(gameData, player, ability.sourceCard(), targetEntry, costText, exileIfCountered, onNotPaidEffects);
            }
        } else {
            counterSpell(gameData, player, ability.sourceCard(), targetEntry, costText, exileIfCountered, onNotPaidEffects);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCounterUnlessWaterbendsChoice(GameData gameData, Player player, boolean accepted,
                                                    PendingMayAbility ability) {
        CounterUnlessEffect effect = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessEffect ce
                        && ce.ransomKind() == CounterUnlessEffect.RansomKind.PAY_WATERBEND)
                .map(CounterUnlessEffect.class::cast)
                .findFirst().orElseThrow();
        int amount = effect.ransomMagnitude();
        UUID targetCardId = ability.targetCardId();

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-waterbend target no longer on stack", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())
                || gameQueryService.isProtectedFromCounterBySourceCard(
                gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String costText = "{" + amount + "} using waterbend";
        if (accepted && waterbendPaymentService.canPay(gameData, player.getId(), amount)) {
            waterbendPaymentService.pay(gameData, player.getId(), amount, ability.sourceCard());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + costText + ". ", targetEntry.getCard(), " is not countered."));
        } else {
            counterSpell(gameData, player, ability.sourceCard(), targetEntry, costText, false, List.of());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void counterSpell(GameData gameData, Player player, Card sourceCard, StackEntry targetEntry,
                              String costText, boolean exileIfCountered, List<CardEffect> onNotPaidEffects) {
        UUID counteredControllerId = targetEntry.getControllerId();
        gameData.stack.remove(targetEntry);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, targetEntry);

        // Copies cease to exist per rule 707.10a
        boolean isAbility = targetEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
        if (!targetEntry.isCopy() && !isAbility) {
            if (exileIfCountered) {
                exileService.exileCard(gameData, counteredControllerId, targetEntry.getPhysicalCard());
            } else {
                graveyardService.addCardToGraveyard(gameData, counteredControllerId, targetEntry.getPhysicalCard());
            }
        }

        String suffix = exileIfCountered ? " is countered and exiled." : " is countered.";
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " declines to pay " + costText + ". ", targetEntry.getCard(), suffix));
        log.info("Game {} - {} — spell countered{}", gameData.id, player.getUsername(), exileIfCountered ? " and exiled" : "");

        // Not paid: resolve any rider against the countered spell's controller (Power Sink).
        counterSupport.resolveNotPaidRider(gameData, sourceCard, counteredControllerId, onNotPaidEffects);
    }

    public void handleCounterUnlessDiscardsChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        // Presence check — the effect is a marker; wording differs from counter-unless-pays.
        ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessEffect ce && ce.ransomKind() == CounterUnlessEffect.RansomKind.DISCARD_CARD)
                .findFirst().orElseThrow();

        UUID targetCardId = ability.targetCardId();
        UUID controllerId = ability.controllerId(); // the countered spell's controller — the decision maker

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-discard target no longer on stack", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isProtectedFromCounterBySourceCard(gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    ability.sourceCard().getColor().name().toLowerCase());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    validIndices.add(i);
                }
            }

            if (!validIndices.isEmpty()) {
                // Paying the Ward cost is the controller's own choice — not an opponent-caused discard.
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a card to discard.", 1);

                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " discards a card. ", targetEntry.getCard(), " is not countered."));
                log.info("Game {} - {} accepts counter-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to counter
        }

        // Declined or no cards — counter the spell/ability
        counterUnlessCounter(gameData, ability.sourceCard(), targetEntry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCounterUnlessCollectsEvidenceChoice(GameData gameData, Player player,
                                                           boolean accepted, PendingMayAbility ability) {
        CounterUnlessCollectsEvidenceEffect effect = ability.effects().stream()
                .filter(CounterUnlessCollectsEvidenceEffect.class::isInstance)
                .map(CounterUnlessCollectsEvidenceEffect.class::cast)
                .findFirst().orElseThrow();

        UUID targetCardId = ability.targetCardId();
        UUID controllerId = ability.controllerId();
        StackEntry targetEntry = gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);

        if (targetEntry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())
                || gameQueryService.isProtectedFromCounterBySourceCard(
                gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            int totalManaValue = graveyard == null
                    ? 0
                    : graveyard.stream().mapToInt(Card::getManaValue).sum();
            if (totalManaValue >= effect.minimumManaValue()) {
                gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume = true;
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginMultiGraveyardChoiceWithMinimumManaValue(
                        gameData, controllerId, new ArrayList<>(graveyard), graveyard.size(),
                        effect.minimumManaValue(),
                        "Choose cards from your graveyard to collect evidence "
                                + effect.minimumManaValue() + ".");
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " accepts — choosing cards to collect evidence. ",
                        ability.sourceCard(), ""));
                return;
            }
        }

        counterUnlessCounter(gameData, ability.sourceCard(), targetEntry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }



    public void handleCounterUnlessExilesGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                                         PendingMayAbility ability) {
        ability.effects().stream()
                .filter(CounterUnlessExilesGraveyardEffect.class::isInstance)
                .findFirst().orElseThrow();

        UUID targetCardId = ability.targetCardId();
        StackEntry targetEntry = gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);

        if (targetEntry == null
                || gameQueryService.isUncounterable(gameData, targetEntry.getCard())
                || gameQueryService.isProtectedFromCounterBySourceCard(
                        gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            UUID controllerId = targetEntry.getControllerId();
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            if (graveyard != null && !graveyard.isEmpty()) {
                List<Card> toExile = new ArrayList<>(graveyard);
                graveyard.clear();
                graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, toExile);
                for (Card card : toExile) {
                    exileService.exileCard(gameData, controllerId, card);
                }
            }

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles all cards from their graveyard. ",
                    targetEntry.getCard(), " is not countered."));
        } else {
            counterUnlessCounter(gameData, ability.sourceCard(), targetEntry);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCounterUnlessDiscardsHandChoice(GameData gameData, Player player, boolean accepted,
                                                       PendingMayAbility ability) {
        ability.effects().stream()
                .filter(CounterUnlessEffect.class::isInstance)
                .map(CounterUnlessEffect.class::cast)
                .filter(e -> e.ransomKind() == CounterUnlessEffect.RansomKind.DISCARD_HAND)
                .findFirst().orElseThrow();

        UUID targetCardId = ability.targetCardId();
        UUID controllerId = ability.controllerId();
        StackEntry targetEntry = gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);

        if (targetEntry == null
                || gameQueryService.isUncounterable(gameData, targetEntry.getCard())
                || gameQueryService.isProtectedFromCounterBySourceCard(
                        gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " discards their hand. ", targetEntry.getCard(), " is not countered."));
            discardHandUnlessPaysLifeEffectHandler.discardTargetHand(
                    gameData, controllerId, controllerId, ability.sourceCard());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        counterUnlessCounter(gameData, ability.sourceCard(), targetEntry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCounterUnlessSacrificesChoice(GameData gameData, Player player, boolean accepted,
                                                    PendingMayAbility ability) {
        CounterUnlessSacrificesEffect sacrificeEffect = ability.effects().stream()
                .filter(CounterUnlessSacrificesEffect.class::isInstance)
                .map(CounterUnlessSacrificesEffect.class::cast)
                .findFirst().orElseThrow();

        UUID targetCardId = ability.targetCardId();
        UUID controllerId = ability.controllerId();
        StackEntry targetEntry = gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);

        if (targetEntry == null || gameQueryService.isUncounterable(gameData, targetEntry.getCard())
                || gameQueryService.isProtectedFromCounterBySourceCard(
                gameData, targetEntry.getControllerId(), ability.sourceCard())) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            List<UUID> validIds = battlefield == null
                    ? List.of()
                    : battlefield.stream()
                            .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, permanent, sacrificeEffect.filter()))
                            .map(Permanent::getId)
                            .toList();
            if (!validIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.SacrificePermanentThen(controllerId, ability.sourceCard(), null));
                playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                        "Choose a permanent to sacrifice.");
                log.info("Game {} - {} accepts counter-unless-sacrifice for {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
                return;
            }
        }

        counterUnlessCounter(gameData, ability.sourceCard(), targetEntry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void counterUnlessCounter(GameData gameData, Card sourceCard, StackEntry targetEntry) {
        gameData.stack.remove(targetEntry);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, targetEntry);

        boolean isAbility = targetEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
        if (!targetEntry.isCopy() && !isAbility) {
            graveyardService.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getPhysicalCard());
        }

        GameLog.Builder counterLog = GameLog.builder().card(targetEntry.getCard())
                .text(isAbility ? "'s ability is countered. (" : " is countered. (")
                .card(sourceCard)
                .text(")");
        gameLogService.append(gameData, counterLog.build());
        log.info("Game {} - {} counters {}", gameData.id, sourceCard.getName(), targetEntry.getCard().getName());
    }

    public void handleSacrificeUnlessDiscardChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
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
                    if (predicateEvaluationService.matchesCardPredicate(hand.get(i), effect.discardPredicate(),
                            sourceCard.getId(), gameData, controllerId)) {
                        validIndices.add(i);
                    }
                }
            }

            if (validIndices.size() >= effect.discardCount()) {
                String typeName = effect.discardDescription();
                gameData.discardCausedByOpponent = false;

                if (effect.random()) {
                    // Pillaging Horde: the discard is at random, so no player choice is needed.
                    playerInteractionSupport.resolveRandomDiscardCards(gameData, controllerId, sourceCard.getName(), effect.discardCount());
                    log.info("Game {} - {} accepts sacrifice-unless-discard (random) for {}", gameData.id, player.getUsername(), sourceCard.getName());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }

                DiscardFollowUp followUp = effect.thenEffect() == null
                        ? DiscardFollowUp.NONE
                        : DiscardFollowUp.thenEffectWithDiscardedManaValue(sourceCard, effect.thenEffect());
                String discardDescription = effect.discardCount() == 1
                        ? "a " + typeName
                        : effect.discardCount() + " " + typeName + "s";
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose " + discardDescription + " to discard.", effect.discardCount(), followUp);

                String logEntry = player.getUsername() + " chooses to discard " + discardDescription + ".";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to discard. ", sourceCard, " is sacrificed."));
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        if (effect.drawCardIfNotDiscarded()) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleExileUnlessDiscardChoice(GameData gameData, Player player, boolean accepted,
            PendingMayAbility ability) {
        ExileUnlessDiscardCardTypeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ExileUnlessDiscardCardTypeEffect)
                .map(e -> (ExileUnlessDiscardCardTypeEffect) e)
                .findFirst().orElseThrow();

        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());

        if (accepted) {
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
                String typeName = effect.requiredType() == null
                        ? "card"
                        : effect.requiredType().name().toLowerCase() + " card";
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " to discard.", 1);
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " chooses to discard a " + typeName + "."));
                return;
            }
        }

        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToExile(gameData, sourcePermanent);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to discard. ", sourceCard, " is exiled."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(gameData, targetPlayerId, validIndices,
                        "Choose a card to discard.", 1);

                String logEntry = player.getUsername() + " chooses to discard a card.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
        } else {
            int lifeLoss = effect.lifeLoss()
                    * gameQueryService.opponentLifeLossMultiplier(gameData, targetPlayerId);
            int currentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, currentLife - lifeLoss);

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " loses " + lifeLoss + " life. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} loses {} life (declined discard, {})", gameData.id, player.getUsername(), lifeLoss, ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLoseLifeUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LoseLifeUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LoseLifeUnlessPaysEffect)
                .map(e -> (LoseLifeUnlessPaysEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();
        boolean penaltyApplied = false;

        if (accepted) {
            ManaCost cost = new ManaCost("{" + effect.payAmount() + "}");
            ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays {" + effect.payAmount() + "}. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to avoid life loss ({})", gameData.id, player.getUsername(), effect.payAmount(), ability.sourceCard().getName());
            } else {
                // Can't pay — apply life loss
                penaltyApplied = true;
                if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                    gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
                } else {
                    int lifeLoss = effect.lifeLoss()
                            * gameQueryService.opponentLifeLossMultiplier(gameData, targetPlayerId);
                    int currentLife = gameData.getLife(targetPlayerId);
                    gameData.playerLifeTotals.put(targetPlayerId, currentLife - lifeLoss);
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " can't pay {" + effect.payAmount() + "}. " + player.getUsername()
                                    + " loses " + lifeLoss + " life. (", ability.sourceCard(), ")"));
                    log.info("Game {} - {} can't pay {} — loses {} life ({})", gameData.id, player.getUsername(), effect.payAmount(), lifeLoss, ability.sourceCard().getName());
                }
            }
        } else {
            // Declined — lose life
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
            } else {
                int lifeLoss = effect.lifeLoss()
                        * gameQueryService.opponentLifeLossMultiplier(gameData, targetPlayerId);
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - lifeLoss);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " loses " + lifeLoss + " life. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} loses {} life (declined to pay, {})", gameData.id, player.getUsername(), lifeLoss, ability.sourceCard().getName());
            }
            penaltyApplied = true;
        }

        if (penaltyApplied && effect.controllerGainsLifeLost() && ability.sourceControllerId() != null) {
            lifeSupport.applyGainLife(gameData, ability.sourceControllerId(), effect.lifeLoss());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDestroyEnchantedPermanentUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect)
                .map(e -> (DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect) e)
                .findFirst().orElseThrow();

        UUID payerId = ability.controllerId();

        if (accepted) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + ability.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to save the enchanted permanent ({})",
                        gameData.id, player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            boolean canPayLife = gameQueryService.canPlayerLifeChange(gameData, payerId)
                    && gameData.getLife(payerId) >= effect.lifeCost();
            if (canPayLife) {
                lifeSupport.applyLifePayment(gameData, payerId, effect.lifeCost(),
                        ability.sourceCard().getName());
                log.info("Game {} - {} pays {} life to save the enchanted permanent ({})",
                        gameData.id, player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can no longer pay either resource — fall through to destruction.
        }

        // Declined (or unable to pay) — destroy the enchanted permanent.
        Permanent aura = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (aura != null && aura.isAttached()) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted != null) {
                destructionSupport.tryDestroyAndLog(gameData, enchanted, ability.sourceCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDamageUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DamageUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DamageUnlessPaysEffect)
                .map(e -> (DamageUnlessPaysEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();

        if (accepted) {
            ManaCost cost = new ManaCost("{" + effect.payAmount() + "}");
            ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays {" + effect.payAmount() + "}. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to avoid damage ({})", gameData.id, player.getUsername(), effect.payAmount(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — fall through to the damage.
        }

        // Declined (or unable to pay) — deal the damage through the normal damage path.
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        if (sourceControllerId == null) {
            // Source left the battlefield — the damage source's controller is the non-target player.
            sourceControllerId = gameData.orderedPlayerIds.stream()
                    .filter(pid -> !pid.equals(targetPlayerId))
                    .findFirst().orElse(targetPlayerId);
        }
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                targetPlayerId, ability.sourcePermanentId());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);

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
            ManaCost cost = new ManaCost(effect.manaCost());
            ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
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
     * Mystic Remora / Rhystic Study two-step choice.
     * <ul>
     *   <li>{@code manaCost != null} — casting opponent's pay phase ({@code targetCardId} = drawing player)</li>
     *   <li>{@code manaCost == null} — source controller's optional-draw confirm</li>
     * </ul>
     */
    public void handleDrawCardUnlessPaysChoice(GameData gameData, Player player, boolean accepted,
                                                PendingMayAbility ability, DrawCardUnlessPaysEffect effect) {
        if (ability.manaCost() != null) {
            // Pay phase — casting opponent decides.
            UUID drawingPlayerId = ability.targetCardId();
            if (accepted) {
                ManaCost cost = new ManaCost(ability.manaCost());
                ManaPool pool = gameData.playerManaPools.get(ability.controllerId());
                if (cost.canPay(pool)) {
                    cost.pay(pool);
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " pays " + ability.manaCost() + ". (", ability.sourceCard(), ")"));
                    log.info("Game {} - {} pays {} to prevent draw ({})",
                            gameData.id, player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                // Accepted but can't pay — fall through to optional draw.
            } else {
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " declines to pay. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} declines to pay for {} draw prevention",
                        gameData.id, player.getUsername(), ability.sourceCard().getName());
            }
            if (drawingPlayerId != null) {
                DrawCardUnlessPaysEffectHandler.offerOptionalDraw(
                        gameData, ability.sourceCard(), effect, drawingPlayerId, ability.sourcePermanentId());
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Draw-confirm phase — source controller decides.
        if (accepted) {
            UUID drawingPlayerId = ability.controllerId();
            for (int i = 0; i < effect.drawCount(); i++) {
                drawService.resolveDrawCard(gameData, drawingPlayerId);
            }
            String drawn = effect.drawCount() == 1 ? "a card" : effect.drawCount() + " cards";
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " draws " + drawn + ". (", ability.sourceCard(), ")"));
            log.info("Game {} - {} draws {} from {}", gameData.id, player.getUsername(),
                    effect.drawCount(), ability.sourceCard().getName());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to draw. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} declines draw from {}", gameData.id, player.getUsername(),
                    ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDamageControllerUnlessDiscardThenTapChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DamageControllerUnlessDiscardThenTapSourceEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DamageControllerUnlessDiscardThenTapSourceEffect)
                .map(e -> (DamageControllerUnlessDiscardThenTapSourceEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();

        if (accepted) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> validIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    validIndices.add(i);
                }
            }

            if (!validIndices.isEmpty()) {
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a card to discard.", 1);

                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " chooses to discard a card. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} accepts damage-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to the damage-then-tap penalty.
        }

        // Declined or no cards — deal the damage and tap the source if it landed.
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), controllerId,
                ability.sourceCard().getName() + "'s ability", new ArrayList<>(),
                null, ability.sourcePermanentId());
        damageControllerUnlessDiscardThenTapSourceEffectHandler.applyDamageThenTapIfDealt(gameData, syntheticEntry, effect.damage());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDiscardHandUnlessPaysLifeChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DiscardHandUnlessPaysLifeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DiscardHandUnlessPaysLifeEffect)
                .map(e -> (DiscardHandUnlessPaysLifeEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();
        UUID casterId = ability.targetCardId();

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)
                && gameData.getLife(targetPlayerId) >= effect.lifeCost();

        if (accepted && canPay) {
            lifeSupport.applyLifePayment(gameData, targetPlayerId, effect.lifeCost(),
                    ability.sourceCard().getName());
            log.info("Game {} - {} pays {} life to keep their hand ({})", gameData.id, player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — discard the whole hand.
            discardHandUnlessPaysLifeEffectHandler.discardTargetHand(gameData, casterId, targetPlayerId, ability.sourceCard());
        }

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

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)
                && gameData.getLife(targetPlayerId) >= effect.lifeCost();
        boolean paid = accepted && canPay;
        if (paid) {
            lifeSupport.applyLifePayment(gameData, targetPlayerId, effect.lifeCost(),
                    ability.sourceCard().getName());
        }

        revealHandDiscardMatchingCardsUnlessPaysLifeEffectHandler.afterCardDecision(
                gameData, ability, effect, targetPlayerId, paid);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Giant Albatross: the damaging creature's controller either pays the life or that creature is
     * destroyed and can't be regenerated; either way the next queued damaging creature is offered.
     */
    public void handleDestroyCreaturesThatDamagedSourceUnlessPaysLifeChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability,
            DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect effect) {
        UUID payingPlayerId = ability.controllerId(); // the damaging creature's controller — decision maker

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, payingPlayerId)
                && gameData.getLife(payingPlayerId) >= effect.lifeCost();
        boolean paid = accepted && canPay;
        if (paid) {
            lifeSupport.applyLifePayment(gameData, payingPlayerId, effect.lifeCost(),
                    ability.sourceCard().getName());
            log.info("Game {} - {} pays {} life to save their creature ({})", gameData.id,
                    player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        }

        destroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffectHandler.afterCreatureDecision(
                gameData, ability, effect, paid);
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
            ManaCost cost = new ManaCost(effect.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payingPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
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

    public void handleStealDyingPermanentUnlessPaysLifeChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        StealDyingOpponentPermanentUnlessPaysLifeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof StealDyingOpponentPermanentUnlessPaysLifeEffect)
                .map(e -> (StealDyingOpponentPermanentUnlessPaysLifeEffect) e)
                .findFirst().orElseThrow();

        UUID payingPlayerId = ability.controllerId(); // "that opponent" — the decision maker
        UUID thiefId = ability.targetCardId();        // the ability controller who steals it

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, payingPlayerId)
                && gameData.getLife(payingPlayerId) >= effect.lifeCost();

        if (accepted && canPay) {
            lifeSupport.applyLifePayment(gameData, payingPlayerId, effect.lifeCost(),
                    ability.sourceCard().getName());
            log.info("Game {} - {} pays {} life to keep their permanent ({})", gameData.id,
                    player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — the thief puts that card onto the battlefield.
            stealDyingOpponentPermanentUnlessPaysLifeEffectHandler.stealPermanent(
                    gameData, thiefId, effect.dyingCardId(), ability.sourceCard());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleTapTargetCreatureUnlessControllerPaysLifeChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TapTargetCreatureUnlessControllerPaysLifeEffect effect = ability.effects().stream()
                .filter(e -> e instanceof TapTargetCreatureUnlessControllerPaysLifeEffect)
                .map(e -> (TapTargetCreatureUnlessControllerPaysLifeEffect) e)
                .findFirst().orElseThrow();

        UUID payingPlayerId = ability.controllerId();    // the target creature's controller — decision maker
        UUID targetPermanentId = ability.targetCardId(); // the creature to tap on decline

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, payingPlayerId)
                && gameData.getLife(payingPlayerId) >= effect.lifeCost();

        if (accepted && canPay) {
            lifeSupport.applyLifePayment(gameData, payingPlayerId, effect.lifeCost(),
                    ability.sourceCard().getName());
            log.info("Game {} - {} pays {} life to avoid the tap ({})", gameData.id,
                    player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — tap the creature.
            UUID abilityControllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
            if (abilityControllerId == null) {
                abilityControllerId = payingPlayerId;
            }
            tapTargetCreatureUnlessControllerPaysLifeEffectHandler.tapTargetCreature(
                    gameData, ability.sourceCard(), abilityControllerId, targetPermanentId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDestroyTargetCreatureUnlessControllerPaysToughnessLifeChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID payingPlayerId = ability.controllerId();    // the target creature's controller — decision maker
        UUID targetPermanentId = ability.targetCardId(); // the creature to destroy on decline

        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            // The creature left the battlefield after the prompt — nothing to pay for or destroy.
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        int lifeCost = destroyTargetCreatureUnlessControllerPaysToughnessLifeEffectHandler.lifeCostFor(gameData, target);
        boolean canPay = lifeCost == 0
                || (gameQueryService.canPlayerLifeChange(gameData, payingPlayerId)
                        && gameData.getLife(payingPlayerId) >= lifeCost);

        if (accepted && canPay) {
            if (lifeCost > 0) {
                lifeSupport.applyLifePayment(gameData, payingPlayerId, lifeCost,
                        ability.sourceCard().getName());
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + lifeCost + " life. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} pays {} life to save their creature ({})", gameData.id,
                    player.getUsername(), lifeCost, ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — destroy the creature; it can't be regenerated.
            destroyTargetCreatureUnlessControllerPaysToughnessLifeEffectHandler.destroyTargetCreature(
                    gameData, ability.sourceCard(), targetPermanentId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleReturnTargetCreatureUnlessControllerPaysChoice(GameData gameData, Player player,
            boolean accepted, PendingMayAbility ability) {
        ReturnTargetCreatureUnlessControllerPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ReturnTargetCreatureUnlessControllerPaysEffect)
                .map(e -> (ReturnTargetCreatureUnlessControllerPaysEffect) e)
                .findFirst().orElseThrow();

        UUID payingPlayerId = ability.controllerId();
        UUID targetPermanentId = ability.targetCardId();
        if (accepted) {
            ManaCost cost = new ManaCost(effect.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payingPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to keep their creature on the battlefield ({})",
                        gameData.id, player.getUsername(), effect.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        UUID abilityControllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        if (abilityControllerId == null) {
            abilityControllerId = payingPlayerId;
        }
        returnTargetCreatureUnlessControllerPaysEffectHandler.returnTargetCreature(
                gameData, ability.sourceCard(), abilityControllerId, targetPermanentId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpponentExileChoice(GameData gameData, Player player, boolean accepted,
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
     * Library of Lat-Nam: the opponent ({@code player}) chose a mode for the spell's controller.
     * Accept schedules "the controller draws three cards at the beginning of the next turn's upkeep";
     * decline puts an unrestricted library search (to hand, then shuffle) onto the stack for the
     * controller. The opponent is the decision maker, so the controller is the other player.
     */
    public void handleLibraryOfLatNamChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) {
            throw new IllegalStateException("Cannot find Library of Lat-Nam controller");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (accepted) {
            gameData.queueDelayedAction(
                    new com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep(controllerId, 3, ability.sourceCard()));
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " draws three cards at the beginning of the next turn's upkeep."));
            log.info("Game {} - {} chooses draw-three for {} (Library of Lat-Nam)", gameData.id, opponentName, controllerName);
        } else {
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " searches their library for a card."));
            log.info("Game {} - {} chooses library search for {} (Library of Lat-Nam)", gameData.id, opponentName, controllerName);
            StackEntry searchEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                    controllerId, ability.sourceCard().getName(),
                    new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect())), 0);
            gameData.stack.add(searchEntry);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Misfortune: the opponent ({@code player}) chooses one of two modes for the spell's controller.
     * Accept is "you put a +1/+1 counter on each creature you control and gain 4 life"; decline is
     * "you put a -1/-1 counter on each creature that player controls and Misfortune deals 4 damage to
     * that player". Both modes ride one stack entry controlled by the spell's controller, so the
     * "each creature you control" / "your opponents' creatures" scopes resolve against them.
     */
    public void handleMisfortuneChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) {
            throw new IllegalStateException("Cannot find Misfortune controller");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        List<CardEffect> effects;
        if (accepted) {
            effects = List.of(
                    new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                            new PermanentIsCreaturePredicate()),
                    new GainLifeEffect(4));
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " puts a +1/+1 counter on each creature they control and gains 4 life."));
        } else {
            effects = List.of(
                    new PutCounterOnEachMatchingPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1,
                            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                    new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                            EachPermanentScope.ALL_PLAYERS),
                    new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT));
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " puts a -1/-1 counter on each creature " + opponentName
                    + " controls and Misfortune deals 4 damage to them."));
        }
        log.info("Game {} - {} chooses {} for {} (Misfortune)", gameData.id, opponentName,
                accepted ? "counters-and-life" : "shrink-and-burn", controllerName);

        StackEntry continuation = new StackEntry(StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                controllerId, ability.sourceCard().getName(), new ArrayList<>(effects), 0);
        continuation.setSpellDamageContinuation(true);
        gameData.stack.add(continuation);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Fatal Lore: the opponent ({@code player}) chooses one of two modes for the spell's controller.
     * Accept is "you draw three cards"; decline is "you destroy up to two creatures that player
     * controls, they can't be regenerated, then that player draws up to three cards". The decline
     * mode's two effects ride one stack entry controlled by the spell's controller, so the destroy
     * choice resumes into the opponent's draw.
     */
    public void handleFatalLoreChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) {
            throw new IllegalStateException("Cannot find Fatal Lore controller");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects;
        if (accepted) {
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " draws three cards."));
            effects = List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(3));
        } else {
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " destroys up to two creatures " + opponentName + " controls, then " + opponentName
                    + " draws up to three cards."));
            effects = List.of(
                    new com.github.laxika.magicalvibes.model.effect.DestroyUpToNCreaturesOpponentControlsEffect(2, true),
                    new com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect(3,
                            com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient.OPPONENT));
        }
        log.info("Game {} - {} chooses mode {} for {} (Fatal Lore)", gameData.id, opponentName,
                accepted ? "draw-three" : "destroy", controllerName);

        gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                controllerId, ability.sourceCard().getName(), new ArrayList<>(effects), 0));

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Covenant of Minds: the targeted opponent ({@code player}) decides for the spell's controller.
     * The revealed cards are still on top of the controller's library. Accept puts those cards into
     * the controller's hand; decline puts them into the controller's graveyard and the controller
     * draws five cards. The opponent is the decision maker, so the controller is the other player.
     */
    public void handleCovenantOfMindsChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID opponentId = ability.controllerId(); // opponent is the decision maker
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(opponentId)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) {
            throw new IllegalStateException("Cannot find Covenant of Minds controller");
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        int count = deck == null ? 0 : Math.min(3, deck.size());
        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            revealed.add(deck.removeFirst());
        }

        if (accepted) {
            for (Card card : revealed) {
                gameData.addCardToHand(controllerId, card);
            }
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: "
                    + controllerName + " puts the " + revealed.size() + " revealed card(s) into their hand."));
            log.info("Game {} - {} lets {} keep {} revealed card(s) (Covenant of Minds)",
                    gameData.id, opponentName, controllerName, revealed.size());
        } else {
            for (Card card : revealed) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
            }
            for (int i = 0; i < 5; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(opponentName + " declines: the "
                    + revealed.size() + " revealed card(s) go to " + controllerName + "'s graveyard and they draw five cards."));
            log.info("Game {} - {} declines; {} mills {} and draws five (Covenant of Minds)",
                    gameData.id, opponentName, controllerName, revealed.size());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Combustible Gearhulk: the targeted opponent chooses whether the controller draws three cards
     * or mills three cards and the source deals damage to that opponent equal to the mana values of
     * the cards that were actually milled.
     */
    public void handleCombustibleGearhulkChoice(GameData gameData, Player player, boolean accepted,
            PendingMayAbility ability) {
        ability.effects().stream()
                .filter(CombustibleGearhulkEffect.class::isInstance)
                .findFirst()
                .orElseThrow();

        UUID targetPlayerId = ability.targetCardId();
        UUID controllerId = ability.sourceControllerId();
        if (controllerId == null) {
            controllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        }
        if (controllerId == null || targetPlayerId == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(targetPlayerId);
        if (accepted) {
            for (int i = 0; i < 3 && gameData.status != GameStatus.FINISHED; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " draws three cards (Combustible Gearhulk)."));
        } else {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, 3);
            int damageAmount = milled.stream().mapToInt(Card::getManaValue).sum();
            if (damageAmount > 0) {
                DealDamageToPlayersEffect damage =
                        new DealDamageToPlayersEffect(damageAmount, DamageRecipient.TARGET_PLAYER);
                StackEntry damageEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), controllerId,
                        ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                        targetPlayerId, ability.sourcePermanentId());
                dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
            }
            gameLogService.append(gameData, GameLog.text(opponentName + " declines: " + controllerName
                    + " mills " + milled.size() + " cards and " + ability.sourceCard().getName()
                    + " deals " + damageAmount + " damage to them."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeUnlessReturnOwnPermanentChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
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
                    if (p.getCard().hasType(effect.permanentType())
                            && !(effect.excludeSource() && p.getCard().getId().equals(sourceCard.getId()))) {
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
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts sacrifice-unless-return for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Battlefield changed since trigger — no valid permanents left, fall through to sacrifice
        }

        // Declined or no valid permanents left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to return a permanent. ", sourceCard, " is sacrificed."));
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to return a permanent.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSacrificeUnlessReturnPermanentChoice(GameData gameData, Player player, boolean accepted,
                                                            PendingMayAbility ability) {
        SacrificeUnlessReturnPermanentTypeToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessReturnPermanentTypeToHandEffect)
                .map(SacrificeUnlessReturnPermanentTypeToHandEffect.class::cast)
                .findFirst().orElseThrow();

        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();
        Permanent sourcePermanent = null;
        List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(controllerId);
        if (controllerBattlefield != null) {
            sourcePermanent = controllerBattlefield.stream()
                    .filter(permanent -> permanent.getCard().getId().equals(sourceCard.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (accepted) {
            List<Permanent> validPermanents = new ArrayList<>();
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getCard().hasType(effect.permanentType())) {
                        validPermanents.add(permanent);
                    }
                }
            }
            if (!validPermanents.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.BouncePermanentOrSacrificeSelf(controllerId, sourceCard.getId()));
                playerInputService.beginPermanentChoice(gameData, controllerId,
                        validPermanents.stream().map(Permanent::getId).toList(),
                        "Choose an " + effect.permanentType().name().toLowerCase()
                                + " to return to its owner's hand.");
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " chooses to return an " + effect.permanentType().name().toLowerCase() + " to hand."));
                return;
            }
        }

        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to return a permanent. ", sourceCard, " is sacrificed."));
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Sacred Mesa: the controller may sacrifice a permanent matching the effect's filter instead of
     * sacrificing the source. Declining — or having nothing left to sacrifice — sacrifices the source.
     */
    public void handleSacrificeUnlessSacrificeOwnPermanentChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SacrificeUnlessSacrificeOwnPermanentEffect effect = ability.effects().stream()
                .filter(e -> e instanceof SacrificeUnlessSacrificeOwnPermanentEffect)
                .map(e -> (SacrificeUnlessSacrificeOwnPermanentEffect) e)
                .findFirst().orElseThrow();

        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        Permanent sourcePermanent = null;
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        if (accepted) {
            List<UUID> validIds = new ArrayList<>();
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.filter())) {
                        validIds.add(p.getId());
                    }
                }
            }

            if (!validIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.SacrificeOwnPermanentOrSacrificeSelf(controllerId, sourceCard.getId()));
                playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                        "Choose " + effect.description() + " to sacrifice.");

                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " chooses to sacrifice " + effect.description() + "."));
                log.info("Game {} - {} accepts sacrifice-unless-sacrifice for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Battlefield changed since the trigger — nothing valid left, fall through to sacrifice
        }

        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to sacrifice " + effect.description() + ". ", sourceCard, " is sacrificed."));
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " declines to sacrifice " + effect.description() + "."));
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleForcedCostOrElseOptionalChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ForcedCostOrElseEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ForcedCostOrElseEffect)
                .map(e -> (ForcedCostOrElseEffect) e)
                .findFirst().orElseThrow();

        // For normal ForcedCostOrElse the deciding player is the source controller. For
        // anyPlayerMayPay / payerIsEnchantedController, ability.controllerId is the payer being
        // asked and forcedCostOrElseSourceControllerId holds the real source controller.
        UUID decidingPlayerId = ability.controllerId();
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId()
                : (effect.anyPlayerMayPay() || effect.payerIsEnchantedController()
                || effect.payerIsDefendingPlayer())
                && gameData.forcedCostOrElseSourceControllerId != null
                ? gameData.forcedCostOrElseSourceControllerId
                : decidingPlayerId;

        if (accepted && effect.forcedCost() instanceof PayLifeCost payLifeCost) {
            int lifeAmount = effectiveLifeAmount(gameData, decidingPlayerId, ability, payLifeCost);
            boolean canPay = lifeAmount <= 0
                    || (gameQueryService.canPlayerLifeChange(gameData, decidingPlayerId)
                            && gameData.getLife(decidingPlayerId) >= lifeAmount);
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
            if (energy >= energyCost.amount()) {
                gameData.playerEnergyCounters.put(decidingPlayerId, energy - energyCost.amount());
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + energyCost.amount()
                                + " energy counter(s). (", ability.sourceCard(), ")"));
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
            forcedCostOrElseEffectHandler.payFlipCoins(
                    gameData, decidingPlayerId, ability.sourceCard(), flipCost.count());
            clearAnyPlayerPayState(gameData);
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
            boolean canPayLife = lifeAmount <= 0
                    || (gameQueryService.canPlayerLifeChange(gameData, decidingPlayerId)
                            && gameData.getLife(decidingPlayerId) >= lifeAmount);
            if (cost.canPay(pool) && canPayLife) {
                var manaBefore = pool.getAllManaTotals();
                cost.pay(pool);
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
                gameData, sourceControllerId, lifeCost, ability.sourceCard(), StackEntryType.TRIGGERED_ABILITY)) {
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
            // The counters go on the source itself, so nothing can make this cost unpayable.
            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", List.of(effect),
                    ability.targetCardId(), ability.sourcePermanentId());
            forcedCostOrElseEffectHandler.payCounterOnSourceCost(gameData, counterEntry, counterCost);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost counterCost) {
            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
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
                            || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)));
            if (!candidates.isEmpty()) {
                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
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
                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
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
            List<UUID> candidates =
                    destructionSupport.collectPermanentIdsWithAnyCounter(gameData, sourceControllerId);
            if (!candidates.isEmpty()) {
                // More than one candidate pauses for a permanent choice, whose completion continues
                // the game itself — only auto-pass when the counter came off immediately.
                forcedCostOrElseEffectHandler.removeCounterFromChosenPermanent(gameData, sourceControllerId,
                        candidates, ability.sourceCard(), ability.sourcePermanentId(), effect);
                if (candidates.size() == 1) {
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
            // payerIsEnchantedController: deciding player is the stack-target payer, not the source
            // controller (Pillar Tombs of Aku / Mind Whip-shaped each-upkeep sacrifice).
            UUID sacrificingPlayerId = effect.payerIsEnchantedController() || effect.payerIsDefendingPlayer()
                    ? decidingPlayerId : sourceControllerId;
            UUID sourcePermanentId = ability.sourcePermanentId();
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(ability.sourceCard().getId())
                    .withSourceControllerId(sourceControllerId)
                    .withSourcePermanentSnapshot(ability.sourcePermanentSnapshot())
                    .withSourcePermanentId(sourcePermanentId);
            List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, sacrificingPlayerId,
                    p -> (!sacrificeCost.excludeSource() || !p.getId().equals(sourcePermanentId))
                            && predicateEvaluationService.matchesPermanentPredicate(p, sacrificeCost.filter(),
                            filterContext));

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
            // Accepted but nothing left to sacrifice — fall through to the penalty.
        }

        // Declined (or unable to pay) — for any-player, offer the next player before the penalty.
        if (effect.anyPlayerMayPay() && offerNextAnyPlayerPay(gameData, ability, effect)) {
            return;
        }

        // Declined (or unable to pay) — resolve the fallback/penalty effects.
        // Preserve targetCardId so ENCHANTED_PERMANENT_CONTROLLER damage / similar fallthroughs
        // still see the enchanted permanent's controller (Mind Whip).
        clearAnyPlayerPayState(gameData);
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName() + "'s ability", List.of(effect),
                ability.targetCardId(), ability.sourcePermanentId());
        syntheticEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        destructionSupport.resolveForcedCostElseEffects(gameData, syntheticEntry, effect);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private int effectiveLifeAmount(GameData gameData, UUID payerId, PendingMayAbility ability,
            PayLifeCost cost) {
        Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        int sourceCounterCount = cost.perSourceCounter() == null || source == null
                ? 0
                : source.getCounterCount(cost.perSourceCounter());
        return cost.effectiveAmount(gameData.getLife(payerId), sourceCounterCount);
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

    public void handleDiscardUnlessExileChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DiscardUnlessExileCardFromGraveyardEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DiscardUnlessExileCardFromGraveyardEffect)
                .map(e -> (DiscardUnlessExileCardFromGraveyardEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();

        if (accepted) {
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            List<Integer> matchingIndices = new ArrayList<>();
            if (graveyard != null) {
                for (int i = 0; i < graveyard.size(); i++) {
                    if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), effect.predicate(), null)) {
                        matchingIndices.add(i);
                    }
                }
            }

            if (!matchingIndices.isEmpty()) {
                // Clear pending effect resolution state — the exile graveyard choice
                // handler calls resolveAutoPass rather than sbaProcessMayAbilitiesThenAutoPass,
                // but there are no remaining effects to resume anyway.
                gameData.pendingEffectResolutionEntry = null;
                gameData.pendingEffectResolutionIndex = 0;

                String filterLabel = CardPredicateUtils.describeFilter(effect.predicate());
                interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                        .builder(controllerId, matchingIndices, GraveyardChoiceDestination.EXILE,
                                "Choose a " + filterLabel + " to exile from your graveyard.")
                        .exileRemainingCount(1)
                        .build());

                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " chooses to exile a " + filterLabel + " from their graveyard. (",
                        ability.sourceCard(), ")"));
                log.info("Game {} - {} accepts exile-from-graveyard for {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
                return;
            }
            // Fall through — no matching cards anymore
        }

        // Declined or no matching cards — discard
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null && !hand.isEmpty()) {
            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, controllerId, 1);

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " must discard a card. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} declines exile, must discard for {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            return;
        }

        // No cards in hand either — nothing happens
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " has no cards to discard. (", ability.sourceCard(), ")"));
        log.info("Game {} - {} has no cards to discard for {}", gameData.id,
                player.getUsername(), ability.sourceCard().getName());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleDiscardUnlessReturnLandChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        // Presence check — the effect is a marker.
        ability.effects().stream()
                .filter(e -> e instanceof DiscardUnlessReturnLandToHandEffect)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();

        if (accepted) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            List<UUID> landIds = new ArrayList<>();
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().hasType(CardType.LAND)) {
                        landIds.add(p.getId());
                    }
                }
            }

            if (!landIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(controllerId));
                playerInputService.beginPermanentChoice(gameData, controllerId, landIds,
                        "Choose a land to return to its owner's hand.");

                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " chooses to return a land to their hand. (", ability.sourceCard(), ")"));
                log.info("Game {} - {} accepts discard-unless-return-land for {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
                return;
            }
            // Fall through — no lands anymore, the discard is mandatory.
        }

        // Declined or no lands — discard a card.
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null && !hand.isEmpty()) {
            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, controllerId, 1);

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " must discard a card. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} declines return, must discard for {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            return;
        }

        // No cards to discard either — nothing happens.
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " has no cards to discard. (", ability.sourceCard(), ")"));
        log.info("Game {} - {} has no cards to discard for {}", gameData.id,
                player.getUsername(), ability.sourceCard().getName());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
