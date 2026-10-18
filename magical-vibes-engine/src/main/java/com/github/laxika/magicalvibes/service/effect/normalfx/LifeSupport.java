package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.replacement.LifeGainEvent;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.LichDuelMasteryLifeLossReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EnduringAngelLifeTotalReplacementEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
        // Rain of Gore turns the whole gain event into an equal life loss. Tainted Remedy and Plague Drone
        // do the same as an ordered life-gain replacement below.
        if (amount > 0 && gameQueryService.lifeGainFromSpellOrAbilityBecomesLifeLoss(
                gameData, controllerId, sourceControllerId)) {
            applyLifeLoss(gameData, controllerId, amount, source != null ? source : "replaced life gain");
            return;
        }
        // Life-gain replacement effects (Heron of Hope, Boon Reflection) replace the amount before it is
        // applied, in the order the gaining player chooses when that order matters (CR 616.1).
        ReplacementOrderService.Outcome<LifeGainEvent> outcome = replacementOrderService.run(gameData,
                new LifeGainEvent(controllerId, amount, Set.of(), source, sourceCard, sourceEntryType, false, false));
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
        if (amount > 0) {
            gameData.lifeGainedThisTurn.merge(controllerId, amount, Integer::sum);
        }

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
            gameData.lifeGainedThisTurn.merge(playerId, gained, Integer::sum);
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
            int lifeLoss = (currentLife - newLife)
                    * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            if (replaceLethalLifeReduction(gameData, playerId, lifeLoss)) {
                return true;
            }
            gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);
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
        amount *= gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        if (replaceLethalLifeReduction(gameData, playerId, amount)) {
            return;
        }
        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, amount);
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
            for (int i = 0; i < amount; i++) {
                gameData.addToExile(playerId, deck.removeFirst());
            }
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + amount + " card(s) instead of paying life (" + sourceName + ")."));
            return;
        }

        int currentLife = gameData.getLife(playerId);
        int lifeLoss = amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        if (replaceLethalLifeReduction(gameData, playerId, lifeLoss)) {
            triggerCollectionService.checkLifePaymentTriggers(gameData, playerId, amount);
            return;
        }
        gameData.playerLifeTotals.put(playerId, currentLife - lifeLoss);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " loses " + lifeLoss + " life (" + sourceName + ")."));
        log.info("Game {} - {} loses {} life from paying for {}", gameData.id, playerName, lifeLoss, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLoss);
        triggerCollectionService.checkLifePaymentTriggers(gameData, playerId, amount);
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
            boolean hasReplacement = angel.getCard().getEffects(EffectSlot.STATIC).stream()
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
        gameData.playerPoisonCounters.put(playerId, currentPoison + amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " poison counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, amount, sourceName);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
    }

    public void applyRadCounters(GameData gameData, UUID playerId, int amount, String sourceName,
                                 UUID placingPlayerId) {
        if (amount <= 0) return;

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
     * controls one that applies.
     *
     * @return true if the life loss was replaced
     */
    public boolean replaceLifeLossWithShield(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;

        List<Permanent> masters = battlefield.stream()
                .filter(source -> !source.isFaceDown())
                .filter(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .anyMatch(LichDuelMasteryLifeLossReplacementEffect.class::isInstance))
                .toList();
        for (Permanent source : masters) {
            ExiledCardEntry shield = gameData.exiledCards.stream()
                    .filter(entry -> source.getId().equals(entry.sourcePermanentId())
                            && playerId.equals(entry.ownerId())
                            && entry.faceDown())
                    .findFirst()
                    .orElse(null);
            if (shield != null && gameData.removeFromExile(shield.card().getId())) {
                gameData.addCardToHand(playerId, shield.card());
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId)
                                + " puts a shield into their hand instead of losing life."));
                return true;
            }
        }

        for (Permanent source : masters) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (gameQueryService.cantBeSacrificed(gameData, source)
                    || !permanentRemovalService.sacrificePermanentToGraveyard(gameData, source)) {
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

    UUID resolveSpellOrAbilityControllerId(GameData gameData) {
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        return pendingEntry != null ? pendingEntry.getControllerId() : null;
    }
}
