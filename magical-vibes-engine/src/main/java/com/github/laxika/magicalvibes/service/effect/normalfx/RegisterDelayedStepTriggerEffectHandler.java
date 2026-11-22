package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.ResolutionResults;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Registers the delayed triggered ability with what the step it is about did, as that is now: each card bound to its
 * stay (CR 603.7c), so the ability's effects read that step as the steps of the resolution that made it do.
 */
@Component
@RequiredArgsConstructor
public class RegisterDelayedStepTriggerEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedStepTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var register = (RegisterDelayedStepTriggerEffect) effect;
        UUID playerId = register.player() == null ? null
                : subjectResolutionService.player(gameData, entry, effect, register.player());
        if (register.player() != null && playerId == null) {
            return;
        }
        StepOutcome bound = switch (register.about()) {
            case EXILE -> exiled(gameData, entry.getResolutionResults().outcome(StepKind.EXILE));
            case SEEK -> sought(gameData, entry.getResolutionResults().outcome(StepKind.SEEK));
            default -> StepOutcome.ofObjects(entry.getCreatedPermanentIds(), List.of());
        };
        if (bound.objectIds().isEmpty() && !register.alsoIfNothingHappened()) {
            return;
        }
        List<CardEffect> effects = register.effects();
        TriggerOptions options = TriggerOptions.DEFAULT;
        if (register.ifAnyStillExiled()) {
            Zone stayedIn = register.about() == StepKind.SEEK ? Zone.HAND : Zone.EXILE;
            Map<UUID, Long> stays = bound.movedCards().stream()
                    .filter(moved -> moved.zone() == stayedIn)
                    .collect(Collectors.toMap(MovedCard::cardId, MovedCard::entryVersion, (first, second) -> first));
            options = options.withInterveningIf(new CardsStillInZone(stayedIn, stays));
            if (effects.size() > 1) {
                // The "if" is checked once as the ability resolves (CR 603.4), not again after each of its steps.
                effects = List.of(new SequenceEffect(effects));
            }
        }
        boolean cards = register.about() != StepKind.CREATE_TOKEN;
        DelayedReferents referents = (cards
                ? DelayedReferents.cards(bound.movedCards().stream().map(MovedCard::cardId).distinct().toList())
                : DelayedReferents.permanents(bound.objectIds()))
                .withStepResults(ResolutionResults.EMPTY.withOutcome(register.about(), bound));
        // "Exile it": an ability about one permanent records it as that permanent.
        UUID permanentId = !cards && bound.objectIds().size() == 1 ? bound.objectIds().getFirst() : null;
        if (register.afterThisTurn()) {
            referents = referents.afterTurn(gameData.turnNumber);
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(register.step(), options, effects.toArray(CardEffect[]::new)),
                entry.getSourcePermanentId(), permanentId, playerId, referents));
    }

    private static StepOutcome sought(GameData gameData, StepOutcome outcome) {
        return StepOutcome.ofObjects(outcome.objectIds(), outcome.playerIds(),
                outcome.movedCards().stream().map(gameData::boundToStay).toList());
    }

    private static StepOutcome exiled(GameData gameData, StepOutcome outcome) {
        List<MovedCard> cards = new ArrayList<>();
        for (UUID objectId : outcome.objectIds()) {
            List<MovedCard> moved = outcome.movedCards().stream()
                    .filter(card -> card.objectId().equals(objectId)).distinct().toList();
            if (moved.isEmpty()) {
                // A card exiled from another zone keeps its id; a token leaves none.
                moved = List.of(new MovedCard(objectId, objectId, Zone.EXILE, null));
            }
            moved.forEach(card -> cards.add(gameData.boundToStay(card)));
        }
        return new StepOutcome(outcome.amount(), outcome.objectIds(), outcome.playerIds(), cards, List.of(),
                outcome.lastKnownPermanents(), outcome.fromHand());
    }
}
