package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collector for the filtered land-play abilities ("whenever [a player] plays a land [from exile]",
 * {@link #collectLandPlayAbility}): its filters are judged here and the ability goes to the stack door whole; the
 * unfiltered ones are {@code TriggerCollectionService}'s. A "whenever you play a card …" ability is written
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

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    /**
     * Collects {@code ability}, whose land-play trigger {@code trigger} narrows the land played by a filter or by the
     * zone it was played from, for the land play {@code landPlayed}: the trigger's filters and its event condition are
     * judged as the land is played, and the stack door judges its intervening "if" (CR 603.4; checked again as it
     * resolves) and limit and puts it on the stack as one object (CR 603.3), choosing its targets as it goes there
     * (CR 603.3d). The record names the player who played the land as the non-targeting "that player" and the land as
     * the triggering card ("that card", "that card's name"), whatever the effects are, with the source as it triggered
     * (what a transform instruction compares, CR 701.27f). The land played is the triggering card the event condition
     * and intervening "if" read ("if it shares a mana value"); one the card writes as an effect condition on the
     * triggering card ("if it shares a card type with the exiled card") is the card's own effect, judged as it resolves.
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
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        UUID sourceCardId = match.permanent() != null ? match.permanent().getOriginalCard().getId()
                : match.sourceCard().getId();
        CardPredicate filter = trigger.filter();
        if (filter != null && !predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), filter,
                sourceCardId, match.gameData(), match.controllerId())) {
            return false;
        }
        ConditionContext context = (match.permanent() != null
                ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                : ConditionContext.forCard(sourceCard, match.controllerId()))
                .withTriggeringCard(landPlayed.landCard());
        if (!conditionEvaluationService.eventConditionMet(match.gameData(), ability.options(), context)) {
            return false;
        }
        Permanent source = match.permanent();
        PlanarObject planarObject = match.sourcePlanarObject();
        Card land = landPlayed.landCard();
        boolean triggered = triggeredAbilityStackService.put(match.gameData(), new TriggeredAbilityRequest(ability,
                sourceCard, source, match.controllerId(), sourceCard.getName() + "'s ability", context, entry -> {
                    entry.setTargetId(landPlayed.playingPlayerId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringCardId(land.getId());
                    entry.setTriggeringCards(List.of(land));
                    if (source != null) {
                        entry.setSourcePermanentSnapshot(new Permanent(source));
                    }
                    if (planarObject != null) {
                        entry.setSourcePlanarObject(planarObject);
                    }
                }).withEvent(landPlayed));
        if (triggered) {
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

}
