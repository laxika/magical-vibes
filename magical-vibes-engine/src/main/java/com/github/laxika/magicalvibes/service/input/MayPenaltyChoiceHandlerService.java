package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.effect.CounterUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final StateBasedActionService stateBasedActionService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DiscardHandUnlessPaysLifeEffectHandler discardHandUnlessPaysLifeEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.StealDyingOpponentPermanentUnlessPaysLifeEffectHandler stealDyingOpponentPermanentUnlessPaysLifeEffectHandler;
    private final CounterSupport counterSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageControllerUnlessDiscardThenTapSourceEffectHandler damageControllerUnlessDiscardThenTapSourceEffectHandler;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    public void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CounterUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessEffect ce && ce.ransomKind() == CounterUnlessEffect.RansomKind.PAY_MANA)
                .map(e -> (CounterUnlessPaysEffect) e)
                .findFirst().orElseThrow();
        int amount = effect.amount();
        boolean exileIfCountered = effect.exileIfCountered();
        List<CardEffect> onNotPaidEffects = effect.onNotPaidEffects();
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
            ManaCost cost = new ManaCost("{" + amount + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + amount + "}. " + targetEntry.getCard().getName() + " is not countered.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} pays {} to avoid counter", gameData.id, player.getUsername(), amount);
            } else {
                counterSpell(gameData, player, ability.sourceCard(), targetEntry, amount, exileIfCountered, onNotPaidEffects);
            }
        } else {
            counterSpell(gameData, player, ability.sourceCard(), targetEntry, amount, exileIfCountered, onNotPaidEffects);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void counterSpell(GameData gameData, Player player, Card sourceCard, StackEntry targetEntry,
                              int amount, boolean exileIfCountered, List<CardEffect> onNotPaidEffects) {
        UUID counteredControllerId = targetEntry.getControllerId();
        gameData.stack.remove(targetEntry);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, targetEntry);

        // Copies cease to exist per rule 707.10a
        if (!targetEntry.isCopy()) {
            if (exileIfCountered) {
                exileService.exileCard(gameData, counteredControllerId, targetEntry.getCard());
            } else {
                graveyardService.addCardToGraveyard(gameData, counteredControllerId, targetEntry.getCard());
            }
        }

        String suffix = exileIfCountered ? " is countered and exiled." : " is countered.";
        String logEntry = player.getUsername() + " declines to pay {" + amount + "}. " + targetEntry.getCard().getName() + suffix;
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

                String logEntry = player.getUsername() + " discards a card. " + targetEntry.getCard().getName() + " is not countered.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts counter-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to counter
        }

        // Declined or no cards — counter the spell/ability
        counterUnlessDiscardCounter(gameData, ability.sourceCard(), targetEntry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void counterUnlessDiscardCounter(GameData gameData, Card sourceCard, StackEntry targetEntry) {
        gameData.stack.remove(targetEntry);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, targetEntry);

        boolean isAbility = targetEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
        if (!targetEntry.isCopy() && !isAbility) {
            graveyardService.addCardToGraveyard(gameData, targetEntry.getControllerId(), targetEntry.getCard());
        }

        String logEntry = isAbility
                ? targetEntry.getCard().getName() + "'s ability is countered. (" + sourceCard.getName() + ")"
                : targetEntry.getCard().getName() + " is countered. (" + sourceCard.getName() + ")";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                    if (effect.requiredType() == null || hand.get(i).getType() == effect.requiredType()) {
                        validIndices.add(i);
                    }
                }
            }

            if (!validIndices.isEmpty()) {
                String typeName = effect.requiredType() == null ? "card" : effect.requiredType().name().toLowerCase() + " card";
                gameData.discardCausedByOpponent = false;

                if (effect.random()) {
                    // Pillaging Horde: the discard is at random, so no player choice is needed.
                    playerInteractionSupport.resolveRandomDiscardCards(gameData, controllerId, sourceCard.getName(), 1);
                    log.info("Game {} - {} accepts sacrifice-unless-discard (random) for {}", gameData.id, player.getUsername(), sourceCard.getName());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }

                playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                        "Choose a " + typeName + " to discard.", 1);

                String logEntry = player.getUsername() + " chooses to discard a " + typeName + ".";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts sacrifice-unless-discard for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Hand changed since trigger — no valid cards left, fall through to sacrifice
        }

        // Declined or no valid cards left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to discard. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to discard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts lose-life-unless-discard for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
                return;
            }

            // Hand changed since prompt — no cards left, fall through to life loss
        }

        // Declined or no cards — lose life
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
        } else {
            int currentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());

            String logEntry = player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} loses {} life (declined discard, {})", gameData.id, player.getUsername(), effect.lifeLoss(), ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLoseLifeUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LoseLifeUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LoseLifeUnlessPaysEffect)
                .map(e -> (LoseLifeUnlessPaysEffect) e)
                .findFirst().orElseThrow();

        UUID targetPlayerId = ability.controllerId();

        if (accepted) {
            ManaCost cost = new ManaCost("{" + effect.payAmount() + "}");
            ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + effect.payAmount() + "}. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} pays {} to avoid life loss ({})", gameData.id, player.getUsername(), effect.payAmount(), ability.sourceCard().getName());
            } else {
                // Can't pay — apply life loss
                if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
                } else {
                    int currentLife = gameData.getLife(targetPlayerId);
                    gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                    String logEntry = player.getUsername() + " can't pay {" + effect.payAmount() + "}. " + player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} can't pay {} — loses {} life ({})", gameData.id, player.getUsername(), effect.payAmount(), effect.lifeLoss(), ability.sourceCard().getName());
                }
            }
        } else {
            // Declined — lose life
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                String logEntry = player.getUsername() + " loses " + effect.lifeLoss() + " life. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} loses {} life (declined to pay, {})", gameData.id, player.getUsername(), effect.lifeLoss(), ability.sourceCard().getName());
            }
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        player.getUsername() + " pays " + ability.manaCost() + ". (" + ability.sourceCard().getName() + ")"));
                log.info("Game {} - {} pays {} to save the enchanted permanent ({})",
                        gameData.id, player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            boolean canPayLife = gameQueryService.canPlayerLifeChange(gameData, payerId)
                    && gameData.getLife(payerId) >= effect.lifeCost();
            if (canPayLife) {
                gameData.playerLifeTotals.put(payerId, gameData.getLife(payerId) - effect.lifeCost());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        player.getUsername() + " pays " + effect.lifeCost() + " life. (" + ability.sourceCard().getName() + ")"));
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
                String logEntry = player.getUsername() + " pays {" + effect.payAmount() + "}. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

                String logEntry = player.getUsername() + " chooses to discard a card. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            int currentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeCost());
            String logEntry = player.getUsername() + " pays " + effect.lifeCost() + " life. (" + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} pays {} life to keep their hand ({})", gameData.id, player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — discard the whole hand.
            discardHandUnlessPaysLifeEffectHandler.discardTargetHand(gameData, casterId, targetPlayerId, ability.sourceCard());
        }

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
            int currentLife = gameData.getLife(payingPlayerId);
            gameData.playerLifeTotals.put(payingPlayerId, currentLife - effect.lifeCost());
            String logEntry = player.getUsername() + " pays " + effect.lifeCost() + " life. ("
                    + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} pays {} life to keep their permanent ({})", gameData.id,
                    player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            // Declined (or can no longer pay) — the thief puts that card onto the battlefield.
            stealDyingOpponentPermanentUnlessPaysLifeEffectHandler.stealPermanent(
                    gameData, thiefId, effect.dyingCardId(), ability.sourceCard());
        }

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
                String logEntry = opponentName + " allows it. " + controllerName + " puts " + exiledCard.getName() + " into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} allows exile return, {} gets {}", gameData.id, opponentName, controllerName, exiledCard.getName());
            }
        } else {
            // Opponent declines — controller draws cards
            int drawCount = effect.drawCount();
            for (int i = 0; i < drawCount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }

            String logEntry = opponentName + " declines. " + controllerName + " draws " + drawCount + " cards.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " chooses: " + controllerName
                    + " draws three cards at the beginning of the next turn's upkeep."));
            log.info("Game {} - {} chooses draw-three for {} (Library of Lat-Nam)", gameData.id, opponentName, controllerName);
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " chooses: " + controllerName
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " chooses: "
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " declines: the "
                    + revealed.size() + " revealed card(s) go to " + controllerName + "'s graveyard and they draw five cards."));
            log.info("Game {} - {} declines; {} mills {} and draws five (Covenant of Minds)",
                    gameData.id, opponentName, controllerName, revealed.size());
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
                    if (p.getCard().hasType(effect.permanentType())) {
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} accepts sacrifice-unless-return for {}", gameData.id, player.getUsername(), sourceCard.getName());
                return;
            }

            // Battlefield changed since trigger — no valid permanents left, fall through to sacrifice
        }

        // Declined or no valid permanents left — sacrifice if still on the battlefield
        if (sourcePermanent != null) {
            permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
            String logEntry = player.getUsername() + " declines to return a permanent. " + sourceCard.getName() + " is sacrificed.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines, {} sacrificed", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            String logEntry = player.getUsername() + " declines to return a permanent.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} is no longer on the battlefield, decline is a no-op", gameData.id, sourceCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handleForcedCostOrElseOptionalChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ForcedCostOrElseEffect effect = ability.effects().stream()
                .filter(e -> e instanceof ForcedCostOrElseEffect)
                .map(e -> (ForcedCostOrElseEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.PayManaCost) {
            // Use the cost stored on the pending ability — it already reflects any dynamic
            // reduction (Draco's Domain) resolved when the prompt was created.
            String costString = ability.manaCost();
            ManaCost cost = new ManaCost(costString);
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays " + costString + ". (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} pays {} to avoid penalty ({})", gameData.id, player.getUsername(), costString, ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost multiCost) {
            List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, multiCost.filter()));
            if (matchingIds.size() >= multiCost.count()) {
                // sacrificePlayerMatchingPermanents sacrifices all when the count matches exactly, or
                // begins a multi-select choice when the controller has more than needed. The choice's
                // completion continues the game itself, so only auto-pass here when nothing is pending.
                destructionSupport.sacrificePlayerMatchingPermanents(gameData, controllerId, multiCost.count(), multiCost.filter());
                if (matchingIds.size() == multiCost.count()) {
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
            // Accepted but no longer enough to sacrifice — fall through to the penalty.
        }

        if (accepted && effect.forcedCost() instanceof SacrificePermanentCost sacrificeCost) {
            List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                    p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacrificeCost.filter()));

            if (matchingIds.size() == 1) {
                Permanent perm = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
                if (perm != null) {
                    destructionSupport.sacrificeAndLog(gameData, perm, controllerId);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
            } else if (matchingIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ForcedCostOrElse(controllerId, ability.sourcePermanentId(),
                                ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                        "Choose a permanent to sacrifice (" + sacrificeCost.description() + ").");
                return;
            }
            // Accepted but nothing left to sacrifice — fall through to the penalty.
        }

        // Declined (or unable to pay) — resolve the fallback/penalty effects.
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), controllerId,
                ability.sourceCard().getName() + "'s ability", List.of(effect),
                null, ability.sourcePermanentId());
        destructionSupport.resolveForcedCostElseEffects(gameData, syntheticEntry, effect);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
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

                String logEntry = player.getUsername() + " chooses to exile a " + filterLabel
                        + " from their graveyard. (" + ability.sourceCard().getName() + ")";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = player.getUsername() + " must discard a card. (" + ability.sourceCard().getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines exile, must discard for {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            return;
        }

        // No cards in hand either — nothing happens
        String logEntry = player.getUsername() + " has no cards to discard. (" + ability.sourceCard().getName() + ")";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} has no cards to discard for {}", gameData.id,
                player.getUsername(), ability.sourceCard().getName());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
