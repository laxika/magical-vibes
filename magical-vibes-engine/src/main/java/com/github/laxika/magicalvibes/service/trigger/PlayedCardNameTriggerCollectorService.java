package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collector for the land-play abilities ("whenever [a player] plays a land"): a filtered one
 * ({@link #collectLandPlayAbility}) is read as an ability, its filters judged here and its effects put on the stack as
 * the card wrote them, and the handlers collect the unfiltered ones. A "whenever you play a card …" ability is written
 * as a cast ability and a land-play ability with the same effects ("play a card" covers both halves, Search the City's
 * ruling); "a card exiled with this" is {@code LandPlayTrigger.exiledWithThis()} on the land-play half.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedCardNameTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * Collects {@code ability}, whose land-play trigger {@code trigger} narrows the land played by a filter or by the
     * zone it was played from, for the land play {@code landPlayed}: the trigger's filters and the intervening "if"
     * (CR 603.4) are judged as the land is played, and the ability goes on the stack as one object (CR 603.3), with
     * "that player" the player who played the land. An intervening "if" is checked again as the ability resolves; one
     * the card writes as an effect condition on the triggering card ("if it shares a card type with the exiled card")
     * is the card's own effect, judged as it resolves. An
     * ability whose effects are bound to a target of the card, or target a permanent, chooses its targets as it
     * triggers (CR 603.3d). The ability's limit is judged by the caller.
     *
     * @return whether the ability triggered
     */
    public boolean collectLandPlayAbility(TriggerMatchContext match, TriggeredAbility ability, LandPlayTrigger trigger,
                                          TriggerContext.LandPlayed landPlayed) {
        if (trigger.playedFrom() != null && landPlayed.playZone() != trigger.playedFrom()
                || trigger.notPlayedFrom() != null && landPlayed.playZone() == trigger.notPlayedFrom()) {
            return false;
        }
        // "A card exiled with this": the land was exiled with the ability's source, the permanent, the face-up plane or
        // the permanent that registered the ability, which the land-play event names since the land has left exile.
        if (trigger.exiledWithSource()) {
            UUID sourceId = match.exiledWithSourceId();
            if (sourceId == null || !sourceId.equals(landPlayed.exiledSourcePermanentId())) {
                return false;
            }
        }
        Card sourceCard = match.sourceCard();
        UUID sourceCardId = match.permanent() != null ? match.permanent().getOriginalCard().getId() : sourceCard.getId();
        CardPredicate filter = trigger.filter();
        if (filter != null && !predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), filter,
                sourceCardId, match.gameData(), match.controllerId())) {
            return false;
        }
        // The event condition ("when you do") is judged only now, the intervening "if" now and again as the ability
        // resolves (CR 603.4), each with the land played as the triggering card ("if it shares a mana value").
        ConditionContext context = (match.permanent() != null
                ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                : ConditionContext.forCard(sourceCard, match.controllerId()))
                .withTriggeringCard(landPlayed.landCard());
        Condition eventCondition = ability.options().eventCondition();
        if (eventCondition != null && !conditionEvaluationService.isMet(match.gameData(), eventCondition, context)) {
            return false;
        }
        Condition interveningIf = ability.options().interveningIf();
        if (interveningIf != null && !conditionEvaluationService.isMet(match.gameData(), interveningIf, context)) {
            return false;
        }
        List<CardEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(effect);
        }
        TargetFilter boundFilter = boundTargetFilter(sourceCard, ability.effects().stream()
                .map(EffectRegistration::effect).toList());
        boolean needsPermanentTarget = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        if (needsPermanentTarget || boundFilter != null) {
            boolean needsPlayerTarget = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), effects, needsPlayerTarget && !needsPermanentTarget,
                    boundFilter, 0, match.permanent() != null ? match.permanent().getId() : null));
            gameLogService.append(match.gameData(), GameLog.cardThen(sourceCard,
                    "'s triggered ability triggers — choose a target."));
            return true;
        }
        return enqueueLandPlayTrigger(match, effects, landPlayed.playingPlayerId(), landPlayed.landCard());
    }

    /**
     * Default for {@link SpellAbilityWatch#ON_CONTROLLER_PLAYS_LAND}: put the ability on the stack
     * whole (e.g. Juju Bubble's {@code SacrificeSelfEffect}), with the land played as the triggering card.
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_ANOTHER_LAND)
    private boolean handleControllerPlaysLandDefault(TriggerMatchContext match, TriggerContext ctx) {
        return enqueueLandPlayTrigger(match, (TriggerContext.LandPlayed) ctx);
    }

    /**
     * Default for {@link SpellAbilityWatch#ON_OPPONENT_PLAYS_LAND}: put the ability on the stack
     * whole (e.g. Dirtcowl Wurm's {@code PutCountersOnSourceEffect}), with the land played as the triggering card.
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND)
    private boolean handleOpponentPlaysLandDefault(TriggerMatchContext match, TriggerContext ctx) {
        return enqueueLandPlayTrigger(match, (TriggerContext.LandPlayed) ctx);
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, TriggerContext.LandPlayed landPlayed) {
        return enqueueLandPlayTrigger(match, match.effects(), landPlayed.playingPlayerId(), landPlayed.landCard());
    }

    /**
     * The filter of the target group the ability's effects are bound to, or {@code null} for none: a "whenever you
     * play a land from exile" ability declares its target as a target group of the card.
     */
    private static TargetFilter boundTargetFilter(Card sourceCard, List<CardEffect> effects) {
        return effects.stream()
                .mapToInt(sourceCard::getEffectTargetIndex)
                .filter(index -> index >= 0 && index < sourceCard.getTargetGroups().size())
                .mapToObj(index -> sourceCard.getTargetGroups().get(index).getFilter())
                .findFirst().orElse(null);
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, List<CardEffect> effects,
                                           UUID playingPlayerId, Card landPlayed) {
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                match.permanent() != null ? match.permanent().getId() : null);
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject());
        }
        entry.setTargetId(playingPlayerId);
        if (landPlayed != null) {
            entry.setTriggeringCardId(landPlayed.getId());
            // "That card", "that card's name", "that card's owner": the land played, as it was played.
            entry.setTriggeringCards(List.of(landPlayed));
        }
        entry.setNonTargeting(true);
        // The source as the ability goes on the stack, so a transform instruction can tell whether it has transformed
        // since (CR 701.27f).
        if (match.permanent() != null) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on playing a land",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
