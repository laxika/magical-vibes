package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.service.effect.OncePerTurnTriggerSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Reads the trigger-only wrapper effects of a triggered ability, the way its family's old slot collector read
 * them, until step 14 of effect-slot-refactor.md turns these wrappers into {@code TriggerOptions} and trigger
 * filters. The trigger pipeline reads every ability's wrappers through this one adapter. What it reads, by the
 * kind of zone change ({@link LegacyTriggerBindings.Kind}):
 *
 * <ul>
 *   <li>"When this dies": the creature-death conditionals, {@code OncePerTurnTriggerEffect}, the death
 *       intervening "if" (CR 603.4) and "put into your graveyard".</li>
 *   <li>"When you sacrifice this": the creature-death conditionals and an intervening "if".</li>
 *   <li>"When this leaves the battlefield": {@code OncePerTurnTriggerEffect}.</li>
 * </ul>
 * Filters that a handler reads together with the event data it binds ("if it had a counter on it", "when this
 * is exiled", "during combat") stay with the handlers in {@link LegacyTriggerBindings}.
 */
@Service
public class LegacyTriggerOptions {

    /**
     * An effect with its trigger-only wrappers taken off.
     *
     * @param effect  what the ability does
     * @param limited the effect whose once-each-turn limit is used up when the ability triggers
     */
    public record Resolved(CardEffect effect, CardEffect limited) {
    }

    private final TriggerCollectionService triggerCollectionService;

    public LegacyTriggerOptions(@Lazy TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * {@code effect}, the one effect of {@code candidate}'s ability, with its wrappers taken off, or {@code null}
     * when they keep it from triggering for {@code event}.
     */
    public Resolved resolve(GameData gameData, LegacyTriggerBindings.Kind kind, ZoneChangeEvent event,
                            TriggerCandidate candidate, CardEffect effect) {
        return switch (kind) {
            case DIES -> death(gameData, event, candidate, effect);
            case SACRIFICED -> sacrifice(gameData, event, candidate, effect);
            case LEAVES -> onceEachTurn(gameData, candidate, effect);
            case ARRIVES -> new Resolved(effect, effect);
        };
    }

    /**
     * Uses up the once-each-turn limit of {@code resolved}, now that its ability triggered (CR 603.2h; a limit
     * marked on acceptance is used up when its controller takes the action).
     */
    public void markLimit(GameData gameData, TriggerCandidate candidate, Resolved resolved) {
        OncePerTurnTriggerSupport.markIfNeeded(gameData, candidate.sourceAsPermanent(), resolved.limited());
    }

    /** Whether {@code effect} is limited to triggering once each turn, so nothing can make it trigger again. */
    public static boolean triggersOnlyOnceEachTurn(CardEffect effect) {
        return effect instanceof OncePerTurnTriggerEffect once
                && !once.firstTokenCreation() && !once.firstLifeGain()
                && !once.firstLandGraveyardEvent() && !once.firstLifeLoss() && !once.firstCycling();
    }

    private Resolved death(GameData gameData, ZoneChangeEvent event, TriggerCandidate candidate,
                           CardEffect effect) {
        Card card = candidate.sourceCard();
        Permanent lastKnown = event.permanent();
        boolean wasCreature = hasType(event, CardType.CREATURE);
        CardEffect unwrapped = triggerCollectionService.unwrapSelfDeathConditional(gameData, card, lastKnown,
                candidate.controllerId(), wasCreature, hasType(event, CardType.LAND), effect);
        if (unwrapped == null) {
            return null;
        }
        CardEffect resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, candidate.sourceAsPermanent(),
                unwrapped);
        if (resolved == null || !triggerCollectionService.passesSelfDeathFilters(gameData, card, lastKnown,
                candidate.controllerId(), wasCreature, resolved)) {
            return null;
        }
        return new Resolved(resolved, unwrapped);
    }

    private Resolved sacrifice(GameData gameData, ZoneChangeEvent event, TriggerCandidate candidate,
                               CardEffect effect) {
        CardEffect resolved = triggerCollectionService.unwrapSelfDeathConditional(gameData,
                candidate.sourceCard(), event.permanent(), candidate.controllerId(),
                hasType(event, CardType.CREATURE), hasType(event, CardType.LAND), effect);
        if (resolved == null || !triggerCollectionService.passesSelfInterveningIf(gameData,
                candidate.sourceAsPermanent(), candidate.controllerId(), resolved)) {
            return null;
        }
        return new Resolved(resolved, resolved);
    }

    private static Resolved onceEachTurn(GameData gameData, TriggerCandidate candidate, CardEffect effect) {
        CardEffect resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, candidate.sourceAsPermanent(),
                effect);
        return resolved == null ? null : new Resolved(resolved, effect);
    }

    /** Whether the moved object had {@code type} as it left: the last-known card holds its effective types. */
    private static boolean hasType(ZoneChangeEvent event, CardType type) {
        Card card = event.permanent() != null ? event.permanent().getCard() : event.card();
        return card.hasType(type);
    }
}
