package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCounteredCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControlledCounterWithExileAndPlayEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared counter-spell helpers used by every "normal" Counter effect handler.
 *
 * <p>Extracted verbatim from {@code CounterResolutionService}; behavior is identical.
 */
@Slf4j
@Component
public class CounterSupport {

    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final StateTriggerService stateTriggerService;
    // @Lazy breaks the cycle: EffectResolutionService → EffectHandlerRegistry →
    // CounterUnlessPaysEffectHandler → CounterSupport → EffectResolutionService.
    private final EffectResolutionService effectResolutionService;

    public CounterSupport(GraveyardService graveyardService,
                          ExileService exileService,
                          GameBroadcastService gameBroadcastService,
                          GameQueryService gameQueryService,
                          StateTriggerService stateTriggerService,
                          @Lazy EffectResolutionService effectResolutionService) {
        this.graveyardService = graveyardService;
        this.exileService = exileService;
        this.gameBroadcastService = gameBroadcastService;
        this.gameQueryService = gameQueryService;
        this.stateTriggerService = stateTriggerService;
        this.effectResolutionService = effectResolutionService;
    }

    public StackEntry findCounterTarget(GameData gameData, UUID targetCardId, StackEntry counterSource) {
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return null;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            return null;
        }

        if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), counterSource)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    counterSource.getCard().getColor().name().toLowerCase());
            return null;
        }

        return targetEntry;
    }

    public void counterSpell(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        boolean isAbility = target.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || target.getEntryType() == StackEntryType.TRIGGERED_ABILITY;

        if (!target.isCopy() && !isAbility) {
            // Guile (CR 614): "If a spell or ability you control would counter a spell, instead exile
            // that spell and you may play that card without paying its mana cost."
            if (applyControlledCounterExileReplacement(gameData, source, target)) {
                return;
            }
            graveyardService.addCardToGraveyard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = isAbility
                ? target.getCard().getName() + "'s ability is countered."
                : target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    public void counterSpellAndPutOnTopOfLibrary(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (!target.isCopy()) {
            // Guile replaces the whole "counter" event: exile and offer a free play.
            if (applyControlledCounterExileReplacement(gameData, source, target)) {
                return;
            }
            gameData.playerDecks.get(target.getControllerId()).add(0, target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered and put on top of its owner's library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} countered {} onto its owner's library", gameData.id,
                source.getCard().getName(), target.getCard().getName());
    }

    /**
     * Desertion (CR): counters {@code target}, then reports whether the countered card is an artifact
     * or creature spell whose card the counter's controller should put onto the battlefield under their
     * control instead of into its owner's graveyard. Returns the countered {@link Card} to gain control
     * of, or {@code null} when it went to the graveyard, was an ability/copy, or was replaced by a
     * controlled-counter effect (Guile).
     */
    public Card counterSpellGainingArtifactOrCreatureControl(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        boolean isAbility = target.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || target.getEntryType() == StackEntryType.TRIGGERED_ABILITY;

        Card gained = null;
        if (!target.isCopy() && !isAbility) {
            // Guile replaces the whole "counter" event: exile and offer a free play.
            if (applyControlledCounterExileReplacement(gameData, source, target)) {
                return null;
            }
            Card spell = target.getCard();
            if (sharesCardType(spell, Set.of(CardType.ARTIFACT, CardType.CREATURE))) {
                gained = spell;
            } else {
                graveyardService.addCardToGraveyard(gameData, target.getControllerId(), spell);
            }
        }

        String logMsg = target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
        return gained;
    }

    public void counterSpellAndExile(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (!target.isCopy()) {
            // Guile replaces the whole "counter" event: exile and offer a free play.
            if (applyControlledCounterExileReplacement(gameData, source, target)) {
                return;
            }
            exileService.exileCard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered and exiled.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} countered and exiled {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    /**
     * Guile's replacement effect: if the controller of the counter effect controls a permanent with
     * {@link ReplaceControlledCounterWithExileAndPlayEffect}, the countered spell is exiled instead of
     * countered and its counter's controller may play it without paying its mana cost. Returns true
     * when the replacement applied (the caller must not run its normal counter disposition).
     */
    private boolean applyControlledCounterExileReplacement(GameData gameData, StackEntry source, StackEntry target) {
        // Only replaces the countering of spells (CR: "would counter a spell"), never abilities.
        boolean isAbility = target.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || target.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
        if (isAbility) {
            return false;
        }

        UUID counterControllerId = source.getControllerId();
        if (!controlsCounterExileReplacement(gameData, counterControllerId)) {
            return false;
        }

        Card spell = target.getCard();
        exileService.exileCard(gameData, target.getControllerId(), spell);
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                spell,
                counterControllerId,
                List.of(new MayPlayExiledCounteredCardEffect()),
                "Play " + spell.getName() + " without paying its mana cost?",
                spell.getId()
        ));

        String logMsg = spell.getName() + " is exiled instead of countered (Guile).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} exiled {} instead of countering (Guile)", gameData.id,
                source.getCard().getName(), spell.getName());
        return true;
    }

    private boolean controlsCounterExileReplacement(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ReplaceControlledCounterWithExileAndPlayEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resolves a counter-unless-pays "not paid" rider (Power Sink) against the countered spell's
     * controller. The rider effects (e.g. tap their lands, empty their mana pool) resolve with their
     * target set to {@code notPayingPlayerId}.
     */
    public void resolveNotPaidRider(GameData gameData, Card sourceCard, UUID notPayingPlayerId,
                                    List<CardEffect> onNotPaidEffects) {
        if (onNotPaidEffects == null || onNotPaidEffects.isEmpty()) {
            return;
        }
        StackEntry riderEntry = new StackEntry(StackEntryType.INSTANT_SPELL, sourceCard, notPayingPlayerId,
                sourceCard.getName(), new ArrayList<>(onNotPaidEffects), 0);
        riderEntry.setTargetId(notPayingPlayerId);
        effectResolutionService.resolveEffects(gameData, riderEntry);
    }

    public boolean sharesCardType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) return true;
        }
        return false;
    }
}
