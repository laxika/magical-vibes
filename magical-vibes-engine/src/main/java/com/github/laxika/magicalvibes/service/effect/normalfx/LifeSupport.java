package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.replacement.LifeGainEvent;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.LichDuelMasteryLifeLossReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnduringAngelLifeTotalReplacementEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Shared life/poison helpers used by every "normal" Life effect handler and by other services
 * (combat, damage, destruction, graveyard return, activated abilities, input handlers).
 *
 * <p>These helpers were extracted verbatim from the original {@code LifeResolutionService} monolith;
 * behavior (log strings, trigger order) is identical.
 */
@Slf4j
@Component
public class LifeSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentRemovalService permanentRemovalService;
    @Lazy
    private final DrawService drawService;
    @Autowired
    @Lazy
    private AnimationSupport animationSupport;
    @Autowired
    @Lazy
    private ReplacementOrderService replacementOrderService;
    @Autowired
    private VisualCueService visualCueService;
    @Autowired
    @Lazy
    private DepartureService departureService;

    public LifeSupport(GameQueryService gameQueryService,
                       GameLogService gameLogService,
                       TriggerCollectionService triggerCollectionService,
                       @Lazy PermanentRemovalService permanentRemovalService,
                       @Lazy DrawService drawService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.permanentRemovalService = permanentRemovalService;
        this.drawService = drawService;
    }

    public void applyGainLife(GameData gameData, UUID controllerId, int amount) {
        applyGainLife(gameData, controllerId, amount, null);
    }

    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source) {
        applyGainLife(gameData, controllerId, amount, source, null, null);
    }

    /**
     * Overload that carries the source card and stack entry type through to life-gain triggers.
     * Used by spell lifelink to let triggers distinguish spell-caused life gain from other sources.
     */
    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source,
                              Card sourceCard, StackEntryType sourceEntryType) {
        applyGainLife(gameData, controllerId, amount, source, sourceCard, sourceEntryType,
                resolveSpellOrAbilityControllerId(gameData));
    }

    /**
     * Applies life gain while preserving the controller of the spell or ability that caused it.
     * The explicit source controller is used by abilities resolved outside the normal stack loop.
     */
    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source,
                              Card sourceCard, StackEntryType sourceEntryType,
                              UUID sourceControllerId) {
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }
        if (!gameQueryService.canPlayerGainLife(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " can't gain life."));
            return;
        }
        if (gameData.playersWhoGainNoLifeThisTurn.contains(controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " gains no life."));
            return;
        }
        if (amount > 0 && hasNefariousLichLifeGainReplacement(gameData, controllerId)
                && !hasOtherLifeGainReplacement(gameData, controllerId)) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }
        // Life-gain replacement effects (Heron of Hope, Boon Reflection, Rain of Gore, Tainted Remedy) replace
        // the event before it is applied, in the order the gaining player chooses when that order matters (CR 616.1).
        ReplacementOrderService.Outcome<LifeGainEvent> outcome = replacementOrderService.run(gameData,
                new LifeGainEvent(controllerId, amount, Set.of(), source, sourceCard, sourceEntryType,
                        sourceControllerId, false, false));
        if (outcome instanceof ReplacementOrderService.Outcome.Completed<LifeGainEvent> completed) {
            completeGainLife(gameData, completed.event());
        }
    }

    /** Gains the life of an event whose replacement effects have all been applied. */
    public void completeGainLife(GameData gameData, LifeGainEvent event) {
        UUID controllerId = event.playerId();
        int amount = event.amount();
        String source = event.source();
        Card sourceCard = event.sourceCard();
        StackEntryType sourceEntryType = event.sourceEntryType();
        if (event.drawInstead()) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }
        if (event.loseInstead()) {
            applyLifeLoss(gameData, controllerId, amount, source != null ? source : "replaced life gain");
            return;
        }
        int currentLife = gameData.getLife(controllerId);
        amount = (int) Math.min((long) amount,
                Math.max(0L, (long) gameData.getMaximumLifeTotal(controllerId) - currentLife));
        if (amount <= 0) {
            return;
        }
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);
        closeDeathReplacementRerunWindow(gameData);
        if (amount > 0) {
            gameData.lifeGainedThisTurn.merge(controllerId, amount, Integer::sum);
        }
        emitLifeCue(gameData, VisualCueType.LIFE_GAINED, controllerId, amount, sourceCard);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = source != null
                ? playerName + " gains " + amount + " life from " + source + "."
                : playerName + " gains " + amount + " life.";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);

        triggerCollectionService.checkLifeGainTriggers(gameData, controllerId, amount, sourceCard, sourceEntryType);
    }

    /**
     * Sets a player's life total per CR 119.5: the change is applied as gaining or losing
     * the necessary amount. Respects canPlayerLifeChange, canPlayerGainLife, and fires triggers.
     *
     * @return true if the life total was changed, false if blocked
     */
    public boolean applySetLifeTotal(GameData gameData, UUID playerId, int newLife) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return false;
        }

        int currentLife = gameData.getLife(playerId);
        newLife = gameData.capLifeTotal(playerId, newLife);
        if (newLife == currentLife) return true;

        if (newLife > currentLife) {
            if (!gameQueryService.canPlayerGainLife(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + " can't gain life."));
                return false;
            }
            if (hasNefariousLichLifeGainReplacement(gameData, playerId)) {
                for (int i = currentLife; i < newLife; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
                return true;
            }
            if (gameQueryService.lifeGainBecomesLifeLoss(gameData, playerId)
                    || gameQueryService.lifeGainFromSpellOrAbilityBecomesLifeLoss(
                            gameData, playerId, resolveSpellOrAbilityControllerId(gameData))) {
                applyLifeLoss(gameData, playerId, newLife - currentLife, "replaced life gain");
                return true;
            }
            int gained = newLife - currentLife;
            gained += gameQueryService.additionalLifeGain(gameData, playerId);
            gained *= gameQueryService.lifeGainMultiplier(gameData, playerId);
            gained = (int) Math.min((long) gained,
                    Math.max(0L, (long) gameData.getMaximumLifeTotal(playerId) - currentLife));
            if (gained <= 0) {
                return true;
            }
            gameData.playerLifeTotals.put(playerId, currentLife + gained);
            closeDeathReplacementRerunWindow(gameData);
            gameData.lifeGainedThisTurn.merge(playerId, gained, Integer::sum);
            emitLifeCue(gameData, VisualCueType.LIFE_GAINED, playerId, gained, null);
            triggerCollectionService.checkLifeGainTriggers(gameData, playerId, gained);
        } else {
            if (!gameQueryService.canPlayerLoseLife(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
                return false;
            }
            if (replaceLifeLossWithShield(gameData, playerId)) {
                return true;
            }
            int lifeLoss = SaturatingMath.multiply(SaturatingMath.subtract(currentLife, newLife),
                    gameQueryService.opponentLifeLossMultiplier(gameData, playerId));
            if (replaceLethalLifeReduction(gameData, playerId, lifeLoss)) {
                return true;
            }
            gameData.playerLifeTotals.put(playerId, SaturatingMath.subtract(currentLife, lifeLoss));
            closeDeathReplacementRerunWindow(gameData);
            emitLifeCue(gameData, VisualCueType.LIFE_LOST, playerId, lifeLoss, null);
            triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLoss);
        }
        return true;
    }

    public void applyLifeLoss(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (amount <= 0) return;
        if (!gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }
        if (replaceLifeLossWithShield(gameData, playerId)) {
            return;
        }
        amount = SaturatingMath.multiply(amount, gameQueryService.opponentLifeLossMultiplier(gameData, playerId));
        if (replaceLethalLifeReduction(gameData, playerId, amount)) {
            return;
        }
        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, SaturatingMath.subtract(currentLife, amount));
        closeDeathReplacementRerunWindow(gameData);
        emitLifeCue(gameData, VisualCueType.LIFE_LOST, playerId, amount, null);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, amount);
    }

    /**
     * Makes the players of {@code losses} lose that life as a step of its own of the resolution that is parked while a
     * choice it offered is completed ("you may cast the copy. If you do, you lose 2 life"): the loss follows the cast
     * (CR 608.2c), and as a resolving step a Lich's Duel Mastery sacrifice replacing it has its death replacements
     * chosen before any life is lost (CR 616.1). The step goes right after the step being resolved while the
     * resolution is still running, and otherwise at the parked resolution's next step. With no resolution to add it
     * to, the losses happen at once.
     */
    public void loseLifeAsStepOfParkedResolution(GameData gameData, Map<UUID, Integer> losses, String sourceName) {
        Map<UUID, Integer> positive = new LinkedHashMap<>();
        losses.forEach((playerId, amount) -> {
            if (amount > 0) {
                positive.merge(playerId, amount, Integer::sum);
            }
        });
        if (positive.isEmpty()) {
            return;
        }
        StackEntry parked = gameData.pendingEffectResolutionEntry;
        if (parked == null) {
            applyLifeLosses(gameData, positive, sourceName);
            return;
        }
        int index = gameData.effectResolutionDepth > 0
                ? parked.getResolvingEffectIndex() + 1
                : gameData.pendingEffectResolutionIndex;
        parked.insertEffectsToResolve(index, List.of(new PlayersLoseLifeEffect(positive)));
    }

    /**
     * Makes each player of {@code losses} lose that much life, in order, as the life losses of one effect. First
     * asks, through {@link #askBeforeLifeLosses}, every question the sacrifices those losses would cause need, so
     * a resolving effect whose later loss would ask can still be stopped and resolved again.
     *
     * @return the total life the players lost
     */
    public int applyLifeLosses(GameData gameData, Map<UUID, Integer> losses, String sourceName) {
        askBeforeLifeLosses(gameData, losses);
        int totalLifeLost = 0;
        for (Map.Entry<UUID, Integer> loss : losses.entrySet()) {
            int lifeBefore = gameData.getLife(loss.getKey());
            applyLifeLoss(gameData, loss.getKey(), loss.getValue(), sourceName);
            totalLifeLost += Math.max(0, lifeBefore - gameData.getLife(loss.getKey()));
        }
        return totalLifeLost;
    }

    /**
     * CR 616.1 for the Lich's Duel Mastery sacrifices that the life losses a resolving effect is about to cause
     * would replace them with ({@link #lifeLossSacrifice}): while that effect has changed nothing yet
     * ({@link GameData#deathReplacementsCanRerunEffect}), asks every death replacement question among them in APNAP
     * order before any life is lost, and stops the effect so it is resolved again with the answers
     * ({@link PermanentRemovalService#askDeathReplacementsBeforeRemoval}). A lone loss would ask by itself inside
     * the window; asking ahead lets every loss of the effect ask, since the first life total that changes closes the
     * window. Does nothing outside the window.
     */
    public void askBeforeLifeLosses(GameData gameData, Map<UUID, Integer> losses) {
        if (!gameData.deathReplacementsCanRerunEffect) {
            return;
        }
        List<Permanent> sacrificed = new ArrayList<>();
        losses.forEach((playerId, amount) -> {
            if (amount <= 0 || !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
                return;
            }
            DepartureIntent sacrifice = lifeLossSacrifice(gameData, playerId);
            Permanent mastery = sacrifice == null ? null
                    : gameQueryService.findPermanentById(gameData, sacrifice.permanentId());
            if (mastery != null) {
                sacrificed.add(mastery);
            }
        });
        if (!sacrificed.isEmpty()) {
            permanentRemovalService.askDeathReplacementsBeforeRemoval(gameData, sacrificed);
        }
    }

    /**
     * {@link #askBeforeLifeLosses} for the life totals a resolving effect is about to set or exchange, each player's
     * to the value of {@code newLifeTotals}: a player whose total would become lower loses the difference (CR 119.5,
     * CR 701.12c), and one whose gain a replacement turns into a loss loses the gain ({@link #applySetLifeTotal}).
     * An effect that sets several life totals one after another needs this before the first, since a total that rises
     * first closes the window before a later one falls.
     */
    public void askBeforeLifeTotalsChange(GameData gameData, Map<UUID, Integer> newLifeTotals) {
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        newLifeTotals.forEach((playerId, newLife) ->
                losses.put(playerId, lifeLossOfSettingLifeTotal(gameData, playerId, newLife)));
        askBeforeLifeLosses(gameData, losses);
    }

    /**
     * For a life total a game-loss replacement is about to set to {@code newLife} (The Golden Throne, Exquisite
     * Archangel, Lich's Mirror): asks the questions of the Lich's Duel Mastery sacrifice the change would cause
     * (CR 616.1f) before the replacement changes anything. Inside a rerun window this stops the resolving effect like
     * {@link #askBeforeLifeTotalsChange}; otherwise the question is asked through {@link DepartureService#askBeforeEvent}
     * and the answered batch waits for the life change's sacrifice. While that question is still open, this keeps
     * reporting it.
     *
     * @return {@code true} when a question is open and the replacement must stop for now
     */
    public boolean askBeforeLossReplacementLifeTotal(GameData gameData, UUID playerId, int newLife) {
        int loss = lifeLossOfSettingLifeTotal(gameData, playerId, newLife);
        if (loss <= 0) {
            return false;
        }
        if (gameData.deathReplacementsCanRerunEffect) {
            askBeforeLifeLosses(gameData, Map.of(playerId, loss));
            return false;
        }
        DepartureIntent sacrifice = gameQueryService.canPlayerLoseLife(gameData, playerId)
                ? lifeLossSacrifice(gameData, playerId) : null;
        if (sacrifice == null) {
            return false;
        }
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending != null && pending.isAwaitingAnswer() && pending.intents().equals(List.of(sacrifice))) {
            return true;
        }
        return departureService.askBeforeEvent(gameData, DepartureBatch.of(List.of(sacrifice)));
    }

    /**
     * {@link #askBeforeLifeLosses} for a resolving effect that makes {@code playerId} gain {@code gain} life and then
     * lose {@code loss} life (Sapling of Colfenor). A gain a replacement turns into a loss puts a Lich's Duel Mastery
     * shield into its controller's hand first, so the loss that follows can be the one the Mastery's sacrifice
     * replaces ({@link #successiveLifeLossSacrifices}). Does nothing outside the window.
     */
    public void askBeforeLifeGainThenLoss(GameData gameData, UUID playerId, int gain, int loss) {
        if (!gameData.deathReplacementsCanRerunEffect || !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            return;
        }
        int gainAsLoss = gain > 0 ? lifeLossOfSettingLifeTotal(gameData, playerId,
                SaturatingMath.add(gameData.getLife(playerId), gain)) : 0;
        int losses = (gainAsLoss > 0 ? 1 : 0) + (loss > 0 ? 1 : 0);
        if (losses == 0) {
            return;
        }
        List<Permanent> masteries = successiveLifeLossSacrifices(gameData, playerId, losses).stream()
                .map(sacrifice -> gameQueryService.findPermanentById(gameData, sacrifice.permanentId()))
                .filter(Objects::nonNull)
                .toList();
        if (!masteries.isEmpty()) {
            permanentRemovalService.askDeathReplacementsBeforeRemoval(gameData, masteries);
        }
    }

    /** The life {@link #applySetLifeTotal} would make {@code playerId} lose now on its way to {@code newLife}. */
    private int lifeLossOfSettingLifeTotal(GameData gameData, UUID playerId, int newLife) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            return 0;
        }
        int currentLife = gameData.getLife(playerId);
        newLife = gameData.capLifeTotal(playerId, newLife);
        if (newLife < currentLife) {
            return SaturatingMath.subtract(currentLife, newLife);
        }
        boolean gainBecomesLoss = newLife > currentLife
                && gameQueryService.canPlayerGainLife(gameData, playerId)
                && !hasNefariousLichLifeGainReplacement(gameData, playerId)
                && (gameQueryService.lifeGainBecomesLifeLoss(gameData, playerId)
                || gameQueryService.lifeGainFromSpellOrAbilityBecomesLifeLoss(
                        gameData, playerId, resolveSpellOrAbilityControllerId(gameData)));
        return gainBecomesLoss ? newLife - currentLife : 0;
    }

    /**
     * {@link #askBeforeLifeLosses} for a resolving effect that makes a change before these life losses, a change
     * that can neither alter their amounts nor what the Mastery sacrifices they cause can be replaced with (a card
     * revealed, or moved from a library to exile). The questions are asked before that change, while the effect
     * can still be resolved again from the start, and the rerun window is then closed, since the change that follows
     * must not happen twice: a loss whose question was missed falls back instead of stopping the effect.
     */
    public void askBeforeLifeLossesThatFollowAChange(GameData gameData, Map<UUID, Integer> losses) {
        askBeforeLifeLosses(gameData, losses);
        closeDeathReplacementRerunWindow(gameData);
    }

    /**
     * {@link #askBeforeLifeLossesThatFollowAChange} for {@code times} life losses (or payments) of {@code amount} that
     * {@code playerId} suffers one after another (each card Sylvan Library or Moonlight Bargain is paid for): each
     * loss puts one of the Lich's Duel Mastery shields left into its controller's hand, so the losses beyond those
     * shields are the ones that sacrifice a Mastery ({@link #replaceLifeLossWithShield}). Their questions are asked
     * before the change that precedes the losses, and the rerun window is then closed.
     */
    public void askBeforeRepeatedLifeLossesThatFollowAChange(GameData gameData, UUID playerId, int amount, int times) {
        if (gameData.deathReplacementsCanRerunEffect && amount > 0 && times > 0
                && gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            List<Permanent> masters = lifeLossShieldSources(gameData, playerId);
            long shields = masters.stream()
                    .mapToLong(source -> gameData.exiledCards.stream()
                            .filter(entry -> isShieldOf(entry, source, playerId))
                            .count())
                    .sum();
            List<Permanent> sacrificed = masters.stream()
                    .filter(source -> !gameQueryService.cantBeSacrificed(gameData, source))
                    .limit(Math.max(0, times - shields))
                    .toList();
            if (!sacrificed.isEmpty()) {
                permanentRemovalService.askDeathReplacementsBeforeRemoval(gameData, sacrificed);
            }
        }
        closeDeathReplacementRerunWindow(gameData);
    }

    /**
     * A life total (or a Lich's Duel Mastery shield) just changed, so a resolving effect stopped later for a death
     * replacement question could not be resolved again from the start without changing it twice.
     */
    private static void closeDeathReplacementRerunWindow(GameData gameData) {
        gameData.deathReplacementsCanRerunEffect = false;
    }

    /** Applies a life payment and fires both life-loss and life-payment triggers. */
    public void applyLifePayment(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (amount <= 0) return;
        if (!gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }
        if (replaceLifeLossWithShield(gameData, playerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (gameQueryService.hasLifePaymentReplacement(gameData, playerId)
                && gameData.getLife(playerId) >= amount
                && deck != null && deck.size() >= amount) {
            closeDeathReplacementRerunWindow(gameData);
            for (int i = 0; i < amount; i++) {
                gameData.addToExile(playerId, deck.removeFirst());
            }
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + amount + " card(s) instead of paying life (" + sourceName + ")."));
            return;
        }

        int currentLife = gameData.getLife(playerId);
        int lifeLoss = SaturatingMath.multiply(amount, gameQueryService.opponentLifeLossMultiplier(gameData, playerId));
        if (replaceLethalLifeReduction(gameData, playerId, lifeLoss)) {
            triggerCollectionService.checkLifePaymentTriggers(gameData, playerId, amount);
            return;
        }
        gameData.playerLifeTotals.put(playerId, SaturatingMath.subtract(currentLife, lifeLoss));
        closeDeathReplacementRerunWindow(gameData);
        emitLifeCue(gameData, VisualCueType.LIFE_LOST, playerId, lifeLoss, null);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " loses " + lifeLoss + " life (" + sourceName + ")."));
        log.info("Game {} - {} loses {} life from paying for {}", gameData.id, playerName, lifeLoss, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLoss);
        triggerCollectionService.checkLifePaymentTriggers(gameData, playerId, amount);
    }

    /**
     * Emits the cosmetic life cue for a change that was just applied. Damage is not life loss here:
     * damage to a player has its own cue, emitted by the damage result stage.
     */
    private void emitLifeCue(GameData gameData, VisualCueType type, UUID playerId, int amount, Card sourceCard) {
        if (visualCueService != null && amount > 0) {
            visualCueService.emit(gameData, type, visualCueService.sourceOf(gameData, sourceCard),
                    null, playerId, amount);
        }
    }

    /**
     * Enduring Angel's replacement of a life reduction: when {@code reduction} would take the player's life
     * total to 0 or less, the angel transforms and the life total becomes 3 instead.
     *
     * @return true if the reduction was replaced and must not be applied
     */
    private boolean replaceLethalLifeReduction(GameData gameData, UUID playerId, int reduction) {
        return reduction > 0 && gameData.getLife(playerId) - reduction <= 0
                && applyEnduringAngelReplacement(gameData, playerId);
    }

    /**
     * Transforms the first Enduring Angel the player controls and makes the player's life total 3.
     *
     * @return true if an angel transformed and the life total was set
     */
    public boolean applyEnduringAngelReplacement(GameData gameData, UUID playerId) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent angel : battlefield) {
            boolean hasReplacement = angel.getCard().getStaticEffects().stream()
                    .anyMatch(EnduringAngelLifeTotalReplacementEffect.class::isInstance);
            if (hasReplacement && animationSupport.transformToBackFace(gameData, angel)) {
                return applySetLifeTotal(gameData, playerId, 3);
            }
        }
        return false;
    }

    public void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName) {
        applyPoisonCounters(gameData, playerId, amount, sourceName, gameData.currentlyResolvingControllerId);
    }

    public void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName,
                                    UUID placingPlayerId) {
        amount = gameQueryService.applyPoisonCounterReplacement(gameData, playerId, amount);
        if (amount <= 0) return;

        amount = gameQueryService.replacePoisonCounters(gameData, playerId, amount, placingPlayerId);
        if (amount <= 0) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, SaturatingMath.add(currentPoison, amount));

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " poison counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, amount, sourceName);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
    }

    public void applyRadCounters(GameData gameData, UUID playerId, int amount, String sourceName,
                                 UUID placingPlayerId) {
        if (amount <= 0 || gameQueryService.playersCantGetCounters(gameData)) return;

        int currentRad = gameData.playerRadCounters.getOrDefault(playerId, 0);
        gameData.playerRadCounters.put(playerId, currentRad + amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " rad counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} rad counter(s) from {}", gameData.id, playerName, amount, sourceName);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
    }

    private boolean hasNefariousLichLifeGainReplacement(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream().anyMatch(permanent ->
                gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                .anyMatch(NefariousLichLifeGainReplacementEffect.class::isInstance));
    }

    private boolean hasOtherLifeGainReplacement(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream().anyMatch(permanent ->
                gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                .anyMatch(LifeGainReplacementEffect.class::isInstance));
    }

    /**
     * Replaces a life loss the player would suffer with Lich's Duel Mastery's shield, if the player
     * controls one that applies, or else by sacrificing the Mastery. The sacrifice is a one-permanent departure
     * batch. It can't stop the life loss in the middle, so it asks its CR 616.1 questions only inside a window that
     * can run its caller again; damage to a player asks them before the damage is dealt
     * ({@link #lifeLossSacrifice}), and the sacrifice then continues that answered batch.
     *
     * @return true if the life loss was replaced
     */
    public boolean replaceLifeLossWithShield(GameData gameData, UUID playerId) {
        List<Permanent> masters = lifeLossShieldSources(gameData, playerId);
        for (Permanent source : masters) {
            ExiledCardEntry shield = shieldFor(gameData, source, playerId);
            if (shield != null && gameData.removeFromExile(shield.card().getId())) {
                closeDeathReplacementRerunWindow(gameData);
                gameData.addCardToHand(playerId, shield.card());
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId)
                                + " puts a shield into their hand instead of losing life."));
                return true;
            }
        }

        for (Permanent source : masters) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (gameQueryService.cantBeSacrificed(gameData, source)) {
                continue;
            }
            DepartureResult result = departureService.departNow(gameData,
                    DepartureBatch.of(List.of(DepartureIntent.sacrifice(source.getId()))),
                    DepartureService.Hooks.NONE).results().getFirst();
            if (!result.sacrificed()) {
                continue;
            }
            if (controllerId != null) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                        gameData, controllerId, source.getCard());
            }
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is sacrificed."));
            permanentRemovalService.removeOrphanedAuras(gameData);
            return true;
        }
        return false;
    }

    /**
     * The sacrifice {@link #replaceLifeLossWithShield} would perform if {@code playerId} lost life now (no shield is
     * left to put into their hand), without changing the game, or {@code null}.
     */
    public DepartureIntent lifeLossSacrifice(GameData gameData, UUID playerId) {
        List<Permanent> masters = lifeLossShieldSources(gameData, playerId);
        if (masters.stream().anyMatch(source -> shieldFor(gameData, source, playerId) != null)) {
            return null;
        }
        return masters.stream()
                .filter(source -> !gameQueryService.cantBeSacrificed(gameData, source))
                .findFirst()
                .map(source -> DepartureIntent.sacrifice(source.getId()))
                .orElse(null);
    }

    /**
     * {@link #lifeLossSacrifice} for {@code amount} life that {@code playerId} is about to lose or pay (paying life is
     * losing it, CR 119.4), or {@code null} when that loss wouldn't happen: no positive amount, or a player who can't
     * lose life. A payment that asks this before any of its costs is paid (CR 601.2h) adds the intent to the batch it
     * asks with {@link DepartureService#askBeforePayment}, and the life payment's sacrifice consumes the answers.
     */
    public DepartureIntent lifeLossSacrifice(GameData gameData, UUID playerId, int amount) {
        if (amount <= 0 || !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            return null;
        }
        return lifeLossSacrifice(gameData, playerId);
    }

    /**
     * The Lich's Duel Mastery sacrifices that {@code losses} separate life losses or payments of {@code playerId},
     * made one after another, would cause ({@link #replaceLifeLossWithShield}), without changing the game: each loss
     * first puts one of the shields left into its controller's hand, and each loss beyond those shields sacrifices
     * the next Mastery that can be sacrificed. Empty for a player who can't lose life. A payment of several life costs
     * asks these before any of its costs is paid (CR 601.2h), so the sacrifice a later payment causes once an earlier
     * one has used the last shield is asked too.
     */
    public List<DepartureIntent> successiveLifeLossSacrifices(GameData gameData, UUID playerId, int losses) {
        if (losses <= 0 || !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            return List.of();
        }
        List<Permanent> masters = lifeLossShieldSources(gameData, playerId);
        long shields = masters.stream()
                .mapToLong(source -> gameData.exiledCards.stream()
                        .filter(entry -> isShieldOf(entry, source, playerId))
                        .count())
                .sum();
        return masters.stream()
                .filter(source -> !gameQueryService.cantBeSacrificed(gameData, source))
                .limit(Math.max(0, losses - shields))
                .map(source -> DepartureIntent.sacrifice(source.getId()))
                .toList();
    }

    private List<Permanent> lifeLossShieldSources(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(source -> !source.isFaceDown())
                .filter(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .anyMatch(LichDuelMasteryLifeLossReplacementEffect.class::isInstance))
                .toList();
    }

    private static ExiledCardEntry shieldFor(GameData gameData, Permanent source, UUID playerId) {
        return gameData.exiledCards.stream()
                .filter(entry -> isShieldOf(entry, source, playerId))
                .findFirst()
                .orElse(null);
    }

    private static boolean isShieldOf(ExiledCardEntry entry, Permanent source, UUID playerId) {
        return source.getId().equals(entry.sourcePermanentId())
                && playerId.equals(entry.ownerId())
                && entry.faceDown();
    }

    UUID resolveSpellOrAbilityControllerId(GameData gameData) {
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        return pendingEntry != null ? pendingEntry.getControllerId() : null;
    }
}
