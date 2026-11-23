package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.EffectRegistration;
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
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
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
 * stay (CR 603.7c), so the ability's effects read that step as the steps of the resolution that made it do. One about
 * a permanent is registered about it, one about a card in a graveyard about that card in that stay, one about no step
 * and no permanent about nothing. Each keeps the number the resolution noted, if it noted one.
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
        if (register.card() != null) {
            registerAboutCard(gameData, entry, register);
            return;
        }
        if (register.permanent() != null) {
            List<UUID> about = subjectResolutionService.permanentIds(gameData, entry, effect, register.permanent());
            if (about.isEmpty()) {
                return;
            }
            UUID permanentId = about.getFirst();
            DelayedReferents referents = DelayedReferents.permanents(List.of(permanentId));
            register(gameData, entry, TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(register.step(), TriggerOptions.DEFAULT,
                            register.effects().toArray(CardEffect[]::new)),
                    entry.getSourcePermanentId(), permanentId, playerId,
                    register.afterThisTurn() ? referents.afterTurn(gameData.turnNumber) : referents));
            return;
        }
        if (register.orStep() != null || register.thisTurn()) {
            List<TriggerCondition> steps = new ArrayList<>(List.of(new TriggerCondition(register.step())));
            if (register.orStep() != null) {
                steps.add(new TriggerCondition(register.orStep()));
            }
            TriggeredAbility ability = new TriggeredAbility(steps, TriggerOptions.DEFAULT,
                    register.effects().stream().map(EffectRegistration::new).toList());
            register(gameData, entry, register.thisTurn()
                    ? TemporaryGlobalTriggeredAbility.nextThisTurn(entry.getControllerId(), entry.getCard(), ability,
                    entry.getSourcePermanentId())
                    : TemporaryGlobalTriggeredAbility.atNextStep(entry.getControllerId(), entry.getCard(), ability,
                    entry.getSourcePermanentId(), null, playerId, DelayedReferents.NONE));
            return;
        }
        if (register.about() == null) {
            register(gameData, entry, TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(register.step(), TriggerOptions.DEFAULT,
                            register.effects().toArray(CardEffect[]::new)),
                    entry.getSourcePermanentId(), null, playerId,
                    register.afterThisTurn() ? DelayedReferents.NONE.afterTurn(gameData.turnNumber)
                            : DelayedReferents.NONE));
            return;
        }
        StepOutcome bound = switch (register.about()) {
            case EXILE -> exiled(gameData, entry.getResolutionResults().outcome(StepKind.EXILE));
            case SEEK -> sought(gameData, entry.getResolutionResults().outcome(StepKind.SEEK));
            case RETURN -> putOntoBattlefield(gameData, entry.getResolutionResults().outcome(StepKind.RETURN));
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
        boolean cards = register.about() == StepKind.EXILE || register.about() == StepKind.SEEK;
        DelayedReferents referents = (cards
                ? DelayedReferents.cards(bound.movedCards().stream().map(MovedCard::cardId).distinct().toList())
                : DelayedReferents.permanents(bound.objectIds()))
                .withStepResults(ResolutionResults.EMPTY.withOutcome(register.about(), bound));
        // "Exile it": an ability about one permanent records it as that permanent.
        UUID permanentId = !cards && bound.objectIds().size() == 1 ? bound.objectIds().getFirst() : null;
        if (register.afterThisTurn()) {
            referents = referents.afterTurn(gameData.turnNumber);
        }
        register(gameData, entry, TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(register.step(), options, effects.toArray(CardEffect[]::new)),
                entry.getSourcePermanentId(), permanentId, playerId, referents));
    }

    /**
     * The ability about the card its subject names in a graveyard, bound to that stay (CR 603.7c, 400.7): the delayed
     * entry records it as its triggering card in that stay. With no such card it is made only when it does more, and
     * then about this card in the stay its entry expected, so it still returns no card that left and came back.
     */
    private void registerAboutCard(GameData gameData, StackEntry entry, RegisterDelayedStepTriggerEffect register) {
        UUID cardId = subjectResolutionService.locatedCards(gameData, entry, register, register.card()).stream()
                .filter(located -> located.zone() == Zone.GRAVEYARD)
                .map(located -> located.card().getId())
                .findFirst().orElse(null);
        DelayedReferents referents;
        if (cardId != null) {
            referents = DelayedReferents.graveyardCard(cardId, gameData.graveyardEntryVersion(cardId));
        } else if (!register.alsoIfNothingHappened()) {
            return;
        } else if (register.card() instanceof CardSubject.SourceCard) {
            referents = DelayedReferents.graveyardCard(entry.getCard().getId(), entry.sourceCardGraveyardEntryVersion());
        } else {
            // "That card" of an entry with no triggering card in its graveyard stay names none.
            referents = DelayedReferents.NONE;
        }
        register(gameData, entry, TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(register.step(), TriggerOptions.DEFAULT,
                        register.effects().toArray(CardEffect[]::new)),
                entry.getSourcePermanentId(), null, null, referents));
    }

    /** Creates {@code delayed}, keeping the number this resolution noted, if any ("where X is the noted number"). */
    private static void register(GameData gameData, StackEntry entry, TemporaryGlobalTriggeredAbility delayed) {
        gameData.temporaryGlobalTriggeredAbilities.add(delayed.keepingNotedAmount(entry.getResolutionResults()));
    }

    /** The permanents the step made that are still there; one that left is a new object (CR 400.7). */
    private static StepOutcome putOntoBattlefield(GameData gameData, StepOutcome outcome) {
        List<UUID> there = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (outcome.objectIds().contains(permanent.getId())) {
                there.add(permanent.getId());
            }
        });
        return StepOutcome.ofObjects(there, outcome.playerIds());
    }

    private static StepOutcome sought(GameData gameData, StepOutcome outcome) {
        return StepOutcome.ofObjects(outcome.objectIds(), outcome.playerIds(),
                outcome.movedCards().stream().map(gameData::boundToStay).toList());
    }

    static StepOutcome exiled(GameData gameData, StepOutcome outcome) {
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
