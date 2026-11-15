package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Triggers the library, draw and discard abilities of a {@link LibraryEvent} (CR 603.2): "whenever you draw a card",
 * "whenever an opponent discards a card", "when you cycle this card", "whenever you scry", "whenever an opponent
 * searches their library" and their kin, each handed whole to the stack door with the record of the event. The
 * abilities are read as the layers leave them ({@link EventAbilitySupport#abilities}: no card ability of a face-down
 * permanent or one that lost them, CR 708.2, 613.1f; granted ones; printed ones through their text changes, CR 612.1),
 * from graveyards ({@link TriggerIndex#graveyardAbilities}), face-up planes and emblems, and matched by their own
 * {@link LibraryTrigger}: its player relation, the drawn, revealed or discarded card's filter, its narrower event ("you
 * cycle a card", "an opponent causes you to discard a card", a draw in or out of a draw step) and which of the
 * player's events this turn triggers it. The door judges the intervening "if" (CR 603.4), the limit (CR 603.2h) and
 * the additional times an ability triggers (a draw's among them, CR 603.3), and chooses targets (CR 603.3d).
 */
@Slf4j
@Service
public class LibraryTriggers {

    private static final LibraryTrigger YOU_DRAW = Triggers.youDrawCard();
    private static final LibraryTrigger OPPONENT_DRAWS = Triggers.opponentDrawsCard();
    private static final LibraryTrigger PLAYER_DRAWS = Triggers.playerDrawsCard();
    private static final LibraryTrigger ENCHANTED_PLAYER_DRAWS = Triggers.enchantedPlayerDrawsCard();
    private static final LibraryTrigger YOU_REVEAL_THIS_WAY = Triggers.youRevealCardThisWay();
    private static final LibraryTrigger OPPONENT_DISCARDS = Triggers.opponentDiscardsCard();
    private static final LibraryTrigger YOU_DISCARD = Triggers.youDiscardCard();
    private static final LibraryTrigger YOU_DISCARD_ONE_OR_MORE = Triggers.youDiscardOneOrMoreCards();
    private static final LibraryTrigger PLAYER_CYCLES = Triggers.playerCyclesCard();
    private static final LibraryTrigger YOU_CYCLE_THIS = Triggers.youCycleThisCard();
    private static final LibraryTrigger YOU_DISCARD_THIS = Triggers.youDiscardThisCard();
    private static final LibraryTrigger OPPONENT_CAUSES_YOU_TO_DISCARD_THIS =
            Triggers.opponentCausesYouToDiscardThisCard();
    private static final LibraryTrigger YOU_SCRY = Triggers.youScry();
    private static final LibraryTrigger OPPONENT_SCRIES = Triggers.opponentScries();
    private static final LibraryTrigger YOU_SURVEIL = Triggers.youSurveil();
    private static final LibraryTrigger OPPONENT_SURVEILS = Triggers.opponentSurveils();
    private static final LibraryTrigger YOU_SEARCH = Triggers.youSearchYourLibrary();
    private static final LibraryTrigger OPPONENT_SEARCHES = Triggers.opponentSearchesTheirLibrary();
    private static final LibraryTrigger PLAYER_SHUFFLES = Triggers.playerShufflesLibrary();
    private static final LibraryTrigger OPPONENT_SHUFFLES = Triggers.opponentShufflesLibrary();
    private static final LibraryTrigger SPELL_OR_ABILITY_CAUSES_SHUFFLE =
            Triggers.spellOrAbilityCausesItsControllerToShuffle();

    /** "A nonland card", the discarded card the "first nonland discard each turn" ability counts. */
    private static final CardPredicate NONLAND_CARD = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    private final EventAbilitySupport abilities;
    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final StateTriggerService stateTriggerService;

    public LibraryTriggers(EventAbilitySupport abilities, TriggerIndex triggerIndex,
                           @Lazy TriggeredAbilityStackService stackService,
                           @Lazy PredicateEvaluationService predicateEvaluationService,
                           @Lazy ConditionEvaluationService conditionEvaluationService,
                           @Lazy GameQueryService gameQueryService, @Lazy GameOutcomeService gameOutcomeService,
                           @Lazy GameLogService gameLogService, @Lazy PlayerInputService playerInputService,
                           @Lazy StateTriggerService stateTriggerService) {
        this.abilities = abilities;
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameQueryService = gameQueryService;
        this.gameOutcomeService = gameOutcomeService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.stateTriggerService = stateTriggerService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, LibraryEvent event) {
        switch (event) {
            case LibraryEvent.Drew drew -> drew(gameData, drew);
            case LibraryEvent.RevealedDrawnCard revealed -> revealedDrawnCard(gameData, revealed);
            case LibraryEvent.Discarded discarded -> discarded(gameData, discarded);
            case LibraryEvent.DiscardedCards discarded -> discardedCards(gameData, discarded);
            case LibraryEvent.Cycled cycled -> cycled(gameData, cycled);
            case LibraryEvent.Scried scried -> scried(gameData, scried);
            case LibraryEvent.Surveilled surveilled -> surveilled(gameData, surveilled);
            case LibraryEvent.Searched searched -> searched(gameData, searched.playerId());
            case LibraryEvent.Shuffled shuffled -> shuffled(gameData, shuffled);
        }
    }

    /**
     * A draw: the drawing player's "whenever you draw a card" abilities (their permanents', their graveyard cards',
     * their emblems'), then the other players' "whenever an opponent draws a card" (permanents', then graveyard
     * cards'), the face-up planes' and the Curses' "whenever enchanted player draws a card".
     */
    private void drew(GameData gameData, LibraryEvent.Drew event) {
        UUID drawingPlayerId = event.playerId();
        Card drawn = event.card();
        watcherDraws(gameData, event, YOU_DRAW, watcher -> watcher.controllerId().equals(drawingPlayerId));
        graveyardDraws(gameData, event, drawingPlayerId, YOU_DRAW, true);
        watcherDraws(gameData, event, OPPONENT_DRAWS, watcher -> !watcher.controllerId().equals(drawingPlayerId));
        watcherDraws(gameData, event, PLAYER_DRAWS, watcher -> true);
        for (UUID graveyardOwnerId : List.copyOf(gameData.playerGraveyards.keySet())) {
            if (!graveyardOwnerId.equals(drawingPlayerId)) {
                graveyardDraws(gameData, event, graveyardOwnerId, OPPONENT_DRAWS, false);
            }
        }
        planarDraws(gameData, event);
        enchantedPlayerDraws(gameData, event);
    }

    /**
     * The draw abilities watched as {@code watched} of the watchers {@code related} relates to the drawing player (its
     * controller's for "you", the others' for "an opponent", everyone's for "a player"): the permanents', with the
     * drawn card judged by the ability's filter, and the emblems' (CR 114.4).
     */
    private void watcherDraws(GameData gameData, LibraryEvent.Drew event, LibraryTrigger watched,
                              java.util.function.Predicate<Watcher> related) {
        UUID drawingPlayerId = event.playerId();
        Card drawn = event.card();
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (!related.test(watcher)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, watched)) {
                Read read = read(ability, watched);
                if (drawEventMatches(event, read) && drawnCardMatches(gameData, watcher, read, drawn,
                        drawingPlayerId)) {
                    putDrawAbility(gameData, ability, watcher, drawingPlayerId, drawn);
                }
            }
        }
    }

    /**
     * The draw abilities watched as {@code watched} of the cards in {@code ownerId}'s graveyard ("your second card each
     * turn", Wolfbat; "their second card each turn", Dogged Detective); "whenever you draw" records this graveyard
     * incarnation of the card as its triggering card (CR 400.7) when {@code recordsItself}.
     */
    private void graveyardDraws(GameData gameData, LibraryEvent.Drew event, UUID ownerId, LibraryTrigger watched,
                                boolean recordsItself) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, watched)) {
                Read read = read(ability, watched);
                requireNoFilter(read, watched);
                if (drawEventMatches(event, read)) {
                    putGraveyardDrawAbility(gameData, ability, card, ownerId, event.playerId(), recordsItself);
                }
            }
        }
    }

    /**
     * The face-up planes' "whenever [you / an opponent] draws a card" abilities, the planar controller's, with the
     * drawn card as their triggering card and the plane as it was.
     */
    private void planarDraws(GameData gameData, LibraryEvent.Drew event) {
        Card drawn = event.card();
        if (gameData.planechase == null || drawn == null || gameData.planechase.controllerId == null) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        LibraryTrigger watched = planarControllerId.equals(event.playerId()) ? YOU_DRAW : OPPONENT_DRAWS;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            Card card = object.getCard();
            for (TriggeredAbility ability : EventAbilitySupport.planarAbilities(card, watched)) {
                Read read = read(ability, watched);
                if (drawEventMatches(event, read) && (read.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(drawn, read.filter(), card.getId(),
                        gameData, event.playerId()))) {
                    putDrawAbility(gameData, ability, card, null, planarControllerId, event.playerId(), drawn,
                            object);
                }
            }
        }
    }

    /** The Curses' "whenever enchanted player draws a card" abilities, the Aura as it was. */
    private void enchantedPlayerDraws(GameData gameData, LibraryEvent.Drew event) {
        UUID drawingPlayerId = event.playerId();
        gameData.forEachBattlefield((auraControllerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (!perm.isAttached() || !drawingPlayerId.equals(perm.getAttachedTo())) continue;
                for (TriggeredAbility ability : abilities.abilities(gameData, perm, ENCHANTED_PLAYER_DRAWS)) {
                    Read read = read(ability, ENCHANTED_PLAYER_DRAWS);
                    requireNothingToJudge(read, ENCHANTED_PLAYER_DRAWS);
                    if (drawEventMatches(event, read)) {
                        putDrawAbility(gameData, ability, perm.getCard(), perm, auraControllerId, drawingPlayerId,
                                null, null);
                    }
                }
            }
        });
    }

    /**
     * {@code source}'s own "whenever you reveal a [card] this way" abilities, when its controller revealed the card it
     * drew by {@code source}'s reveal ability: one for each reveal source (Primitive Etchings' ruling: two reveal the
     * same card and both trigger), the filter judged on the card, the card their triggering card.
     */
    private void revealedDrawnCard(GameData gameData, LibraryEvent.RevealedDrawnCard event) {
        Permanent source = event.source();
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (!event.playerId().equals(controllerId)) {
            return;
        }
        for (TriggeredAbility ability : abilities.abilities(gameData, source, YOU_REVEAL_THIS_WAY)) {
            Read read = read(ability, YOU_REVEAL_THIS_WAY);
            requireNoCount(read, YOU_REVEAL_THIS_WAY);
            if (drawnCardMatches(gameData, Watcher.of(source, controllerId), read, event.card(), event.playerId())) {
                putDrawAbility(gameData, ability, source.getCard(), source, event.playerId(), event.playerId(),
                        event.card(), null);
            }
        }
    }

    /**
     * Whether {@code event} is the draw of {@code read}'s ability: any draw, every draw but the player's first in
     * their draw step, or only that one, and, for "your Nth card each turn", the player's Nth draw this turn (CR 121.1).
     */
    private static boolean drawEventMatches(LibraryEvent.Drew event, Read read) {
        boolean eventMatches = switch (read.event()) {
            case DRAWS, ENCHANTED_PLAYER_DRAWS -> true;
            case DRAWS_EXCEPT_FIRST_IN_DRAW_STEP -> !event.firstInDrawStep();
            case DRAWS_FIRST_IN_DRAW_STEP -> event.firstInDrawStep();
            default -> throw new IllegalStateException("The draw matcher reads no " + read.event() + ": "
                    + read.ability());
        };
        return eventMatches && read.countMatches(event.cardsDrawnThisTurn());
    }

    /**
     * Whether {@code drawn} is the card {@code read}'s ability is about ("whenever you draw a Dragon card", "whenever
     * you reveal a basic land card this way"), with {@code source} as the filter's source: any card when the ability
     * has no filter, and none when the draw has no card.
     */
    private boolean drawnCardMatches(GameData gameData, Watcher source, Read read, Card drawn,
                                     UUID drawingPlayerId) {
        if (read.filter() == null) {
            return true;
        }
        Permanent permanent = source.asPermanent();
        return drawn != null && predicateEvaluationService.matchesCardPredicate(drawn, read.filter(),
                source.card().getId(), gameData, drawingPlayerId, permanent.getId(), null, null, permanent);
    }

    /**
     * Hands {@code ability} of {@code watcher}, a "whenever [a player] draws a card" ability, to the stack door: a
     * permanent's as {@link #putDrawAbility(GameData, TriggeredAbility, Card, Permanent, UUID, UUID, Card,
     * PlanarObject)} does, an emblem's as an emblem's ability (CR 114.4), with the same record.
     */
    private void putDrawAbility(GameData gameData, TriggeredAbility ability, Watcher watcher, UUID drawingPlayerId,
                                Card drawn) {
        if (watcher.emblem() == null) {
            putDrawAbility(gameData, ability, watcher.card(), watcher.permanent(), watcher.controllerId(),
                    drawingPlayerId, drawn, null);
            return;
        }
        abilities.put(gameData, ability, watcher, watcher.conditionContext().withTargetId(drawingPlayerId),
                drawRecord(null, drawingPlayerId, drawn, null));
    }

    /**
     * Hands {@code ability} of {@code sourceCard} ({@code source} on the battlefield, or {@code null} for a face-up
     * plane, {@code planarSource}) to the stack door for {@code drawingPlayerId}'s draw of {@code drawn}: its event
     * condition is judged with the drawing player as "that player", and the draw is the event the "triggers an
     * additional time" effects of draws count (Krang, the All-Powerful). The record names the drawing player as its
     * triggering player and its non-targeting "that player" (CR 115.10a), {@code drawn} as its triggering card, and the
     * source as it was or the plane.
     */
    private void putDrawAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                                UUID controllerId, UUID drawingPlayerId, Card drawn, PlanarObject planarSource) {
        ConditionContext context = (source != null ? ConditionContext.forPermanent(source, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId)).withTargetId(drawingPlayerId);
        put(gameData, ability, sourceCard, source, controllerId, context,
                drawRecord(source, drawingPlayerId, drawn, planarSource), new TriggerCause.Drew());
    }

    /**
     * The record of a draw: the drawing player as the non-targeting "that player" and the triggering player, the drawn
     * card as the triggering card, and the source as it was when the ability triggered (its permanent, or the planar
     * object, when it has one).
     */
    private static TriggerRecord drawRecord(Permanent source, UUID drawingPlayerId, Card drawn,
                                            PlanarObject planarSource) {
        Permanent snapshot = source == null ? null : new Permanent(source);
        PlanarObject plane = planarSource == null ? null : planarSource.copy();
        return entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(drawingPlayerId);
            entry.setTriggeringPlayerId(drawingPlayerId);
            if (drawn != null) {
                entry.setTriggeringCard(drawn.getId(), -1);
                entry.setTriggeringCards(List.of(drawn));
            }
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (plane != null) {
                entry.setSourcePlanarObject(plane);
            }
        };
    }

    /**
     * {@link #putDrawAbility} for {@code ability} of {@code card} in its owner {@code ownerId}'s graveyard: the record
     * names the drawing player as its triggering player and, when {@code recordsItself}, this graveyard incarnation of
     * the card as its triggering card (CR 400.7).
     */
    private void putGraveyardDrawAbility(GameData gameData, TriggeredAbility ability, Card card, UUID ownerId,
                                         UUID drawingPlayerId, boolean recordsItself) {
        ConditionContext context = ConditionContext.forCard(card, ownerId).withTargetId(drawingPlayerId);
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        put(gameData, ability, card, null, ownerId, context, entry -> {
            entry.setNonTargeting(true);
            entry.setTriggeringPlayerId(drawingPlayerId);
            if (recordsItself) {
                entry.setTriggeringCardId(card.getId());
                entry.setTriggeringCardGraveyardEntryVersion(graveyardEntryVersion);
            }
        }, new TriggerCause.Drew());
    }

    /**
     * A discard: the other players' "whenever an opponent discards a card" abilities, the delayed ones of a discard an
     * opponent caused, the discarding player's "whenever you discard a card" (and "you cycle a card", "an opponent
     * causes you to discard a card"), their "whenever you discard one or more cards" for a discard that is an event of
     * its own, then the discarded card's own "when you discard this card" abilities from the graveyard it went to.
     * A card put into a hidden zone unrevealed (Library of Leng) has undefined characteristics (CR 701.9c), so no
     * filtered ability matches it.
     */
    private void discarded(GameData gameData, LibraryEvent.Discarded event) {
        UUID discardingPlayerId = event.playerId();
        Card discardedCard = event.card();
        boolean characteristicsKnown = discardedCard != null
                && !isDiscardedToLibraryUnrevealed(gameData, discardedCard);
        List<Card> cards = discardedCard == null ? List.of() : List.of(discardedCard);
        boolean anyTriggered = false;

        boolean[] opponentsTriggered = {false};
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;
            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, perm, OPPONENT_DISCARDS)) {
                    Read read = read(ability, OPPONENT_DISCARDS);
                    requireWatchedEvent(read, OPPONENT_DISCARDS);
                    requireNoCount(read, OPPONENT_DISCARDS);
                    if (discardedCardMatches(gameData, read, discardedCard, characteristicsKnown, playerId)
                            && putDiscardAbility(gameData, ability, perm, playerId, discardingPlayerId, cards, null)) {
                        opponentsTriggered[0] = true;
                    }
                }
            }
        });
        anyTriggered |= opponentsTriggered[0];

        if (event.causedByOpponent() && temporaryOpponentDiscards(gameData, discardingPlayerId, discardedCard)) {
            anyTriggered = true;
        }

        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, perm, YOU_DISCARD)) {
                    Read read = read(ability, YOU_DISCARD);
                    if (discardedCardMatches(gameData, read, discardedCard, characteristicsKnown, discardingPlayerId)
                            && controllerDiscardEventMatches(gameData, read, event)
                            && putDiscardAbility(gameData, ability, perm, discardingPlayerId, discardingPlayerId,
                            cards, null)) {
                        anyTriggered = true;
                    }
                }
            }
        }

        if (discardedCard != null && event.alone()
                && discardEventAbilities(gameData, discardingPlayerId, List.of(discardedCard))) {
            anyTriggered = true;
        }

        if (anyTriggered) {
            gameOutcomeService.checkWinCondition(gameData);
        }
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        if (discardedCard != null) {
            ownDiscardAbilities(gameData, discardedCard, discardingPlayerId, YOU_DISCARD_THIS, false);
            if (event.causedByOpponent()) {
                ownDiscardAbilities(gameData, discardedCard, discardingPlayerId, OPPONENT_CAUSES_YOU_TO_DISCARD_THIS,
                        true);
            }
        }
    }

    /**
     * The discarded card's own "when you discard this card" or "when a spell or ability an opponent controls causes
     * you to discard this card" abilities, each whole (CR 603.3), as abilities of the graveyard it went to: the record
     * names the card's graveyard stay as its source's and, for the second, this card there as its triggering card
     * (CR 400.7).
     */
    private void ownDiscardAbilities(GameData gameData, Card card, UUID ownerId, LibraryTrigger watched,
                                     boolean recordsItself) {
        for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, watched)) {
            long stay = gameData.graveyardEntryVersion(card.getId());
            put(gameData, ability, card, null, ownerId, ConditionContext.forCard(card, ownerId), entry -> {
                entry.setNonTargeting(true);
                entry.setSourceGraveyardEntryVersion(stay);
                if (recordsItself) {
                    entry.setTriggeringCard(card.getId(), stay);
                }
            }, null);
        }
    }

    /**
     * The turn-scoped delayed "whenever a spell or ability an opponent controls causes you to discard a card" abilities
     * of {@code discardingPlayerId}, with the discarding player as "that player" and the card as the event's card.
     */
    private boolean temporaryOpponentDiscards(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        boolean triggered = false;
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(OPPONENT_DISCARDS)
                    || !watcher.controllerId().equals(discardingPlayerId)) {
                continue;
            }
            Condition interveningIf = watcher.ability().options().interveningIf();
            List<CardEffect> effects = watcher.ability().effects().stream()
                    .map(EffectRegistration::effect)
                    .map(effect -> interveningIf == null ? effect
                            : (CardEffect) new ConditionalEffect(interveningIf, effect))
                    .toList();
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", new ArrayList<>(effects));
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
            if (discardedCard != null) {
                entry.setEventCardIds(List.of(discardedCard.getId()));
            }
            stackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global discard trigger fires", gameData.id,
                    watcher.sourceCard().getName());
            triggered = true;
        }
        return triggered;
    }

    /**
     * A discard event of one or more cards: state triggers are checked first, as the player's hand may just have
     * emptied (CR 603.8), then the discarding player's "whenever you discard one or more cards" abilities.
     */
    private void discardedCards(GameData gameData, LibraryEvent.DiscardedCards event) {
        stateTriggerService.checkStateTriggers(gameData);
        if (discardEventAbilities(gameData, event.playerId(), event.cards())) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * The "whenever you discard one or more cards" abilities of {@code discardingPlayerId}'s permanents for the discard
     * event of {@code cards}: one of the cards matches the ability's filter ("one or more land cards"), and "for the
     * first time each turn" is the player's first discard event this turn, counting those before the source was on the
     * battlefield. The number of cards is the event's value.
     *
     * @return whether one triggered
     */
    private boolean discardEventAbilities(GameData gameData, UUID discardingPlayerId, List<Card> cards) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield == null) {
            return false;
        }
        boolean triggered = false;
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, YOU_DISCARD_ONE_OR_MORE)) {
                Read read = read(ability, YOU_DISCARD_ONE_OR_MORE);
                requireWatchedEvent(read, YOU_DISCARD_ONE_OR_MORE);
                if (read.filter() != null && cards.stream().noneMatch(card -> predicateEvaluationService
                        .matchesCardPredicate(card, read.filter(), null, gameData, discardingPlayerId))) {
                    continue;
                }
                if (!read.counts().isEmpty()) {
                    requireNoCountButFirst(read, YOU_DISCARD_ONE_OR_MORE);
                    if (gameData.cardsDiscardedThisTurn.getOrDefault(discardingPlayerId, 0) != cards.size()) {
                        continue;
                    }
                }
                triggered |= putDiscardAbility(gameData, ability, perm, discardingPlayerId, discardingPlayerId, cards,
                        cards.size());
            }
        }
        return triggered;
    }

    /** Whether {@code read}'s filter, if any, matches {@code discardedCard}, judged for {@code controllerId}. */
    private boolean discardedCardMatches(GameData gameData, Read read, Card discardedCard,
                                         boolean characteristicsKnown, UUID controllerId) {
        if (read.filter() == null) {
            return true;
        }
        return characteristicsKnown && predicateEvaluationService.matchesCardPredicate(discardedCard, read.filter(),
                null, gameData, controllerId);
    }

    /**
     * Whether the discard {@code event} is the event of {@code read}'s "whenever you discard a card" ability: any
     * discard, a cycling ("whenever you cycle a card"), or a discard an opponent's spell or ability causes; and, for
     * "for the first time each turn", the player's first cycling or first nonland discard this turn, counting those
     * before the source was on the battlefield.
     */
    private static boolean controllerDiscardEventMatches(GameData gameData, Read read,
                                                         LibraryEvent.Discarded event) {
        boolean eventMatches = switch (read.event()) {
            case DISCARDS -> true;
            case CYCLES -> event.cycled();
            case DISCARDS_BY_OPPONENT -> event.causedByOpponent();
            default -> throw new IllegalStateException("The \"you discard a card\" matcher reads no "
                    + read.event() + ": " + read.ability());
        };
        if (!eventMatches || read.counts().isEmpty()) {
            return eventMatches;
        }
        requireNoCountButFirst(read, YOU_DISCARD);
        if (read.event() == LibraryTrigger.LibraryEvent.CYCLES && read.filter() == null) {
            return gameData.cardsCycledThisTurn.getOrDefault(event.playerId(), 0) == 1;
        }
        if (read.event() == LibraryTrigger.LibraryEvent.DISCARDS && NONLAND_CARD.equals(read.filter())) {
            return gameData.nonlandCardsDiscardedThisTurn.getOrDefault(event.playerId(), 0) == 1;
        }
        throw new IllegalStateException("The \"you discard a card\" matcher counts only the first cycling and the "
                + "first nonland discard of the turn: " + read.ability());
    }

    /**
     * Whether a discarded card sits in a library instead of a graveyard: a discard replacement (Library of Leng) put
     * it there without revealing it, so its characteristics are undefined (CR 701.9c).
     */
    private static boolean isDiscardedToLibraryUnrevealed(GameData gameData, Card discardedCard) {
        boolean inGraveyard = gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> graveyard.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
        if (inGraveyard) {
            return false;
        }
        return gameData.playerDecks.values().stream()
                .anyMatch(deck -> deck.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
    }

    /**
     * Hands {@code ability} of {@code perm} to the stack door for {@code playerId}'s discard (or cycling, CR 702.29a)
     * of {@code cards}: the record names, whatever the effects are, the player as its triggering player and its
     * non-targeting "that player", the cards as its triggering cards, each with the graveyard stay it went to ("those
     * cards", a card that left the graveyard and returned is a new object, CR 400.7), a lone card also as its
     * triggering card, {@code count} (for "one or more cards") as the event's value and the source as it was.
     *
     * @return whether it triggered
     */
    private boolean putDiscardAbility(GameData gameData, TriggeredAbility ability, Permanent perm,
                                      UUID controllerId, UUID playerId, List<Card> cards, Integer count) {
        Permanent snapshot = new Permanent(perm);
        Map<UUID, Long> stays = new LinkedHashMap<>();
        for (Card card : cards) {
            stays.put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        return put(gameData, ability, perm.getCard(), perm, controllerId,
                ConditionContext.forPermanent(perm, controllerId), entry -> {
                    entry.setNonTargeting(true);
                    entry.setSourcePermanentSnapshot(snapshot);
                    if (count != null) {
                        entry.setEventValue(count);
                    }
                    entry.setTriggeringPlayerId(playerId);
                    if (entry.getTargetId() == null) {
                        entry.setTargetId(playerId);
                    }
                    entry.setTriggeringCards(cards);
                    entry.getEventCardGraveyardEntryVersions().putAll(stays);
                    if (cards.size() == 1) {
                        Card card = cards.getFirst();
                        entry.setTriggeringCard(card.getId(), stays.getOrDefault(card.getId(), 0L));
                    }
                }, null);
    }

    /**
     * A cycling: the cycled card's own "when you cycle this card" abilities, then every permanent's "whenever a player
     * cycles a card". "When you cycle this card" triggers from whatever zone the card winds up in (CR 702.29c), so it
     * is read as the card has it there ({@link #cycledCardAbilities}). Its X is the X paid for the cycling cost; its targets, a spell's among them, are chosen
     * as it goes on the stack (CR 603.3d), but for those a cycling ability that announces this ability's targets
     * announced (Rampaging War Mammoth).
     */
    private void cycled(GameData gameData, LibraryEvent.Cycled event) {
        UUID cyclingPlayerId = event.playerId();
        Card cycledCard = event.card();
        ActivatedAbility targetingCyclingAbility = event.targetingCyclingAbility();
        for (TriggeredAbility ability : cycledCardAbilities(gameData, cycledCard)) {
            List<UUID> announcedTargets = event.announcedTargetIds() != null ? event.announcedTargetIds() : List.of();
            boolean chooseOnTrigger = targetingCyclingAbility != null && announcedTargets.isEmpty()
                    && targetingCyclingAbility.isCyclingTriggerTargetsChosenOnTriggerIfOmitted();
            ConditionContext context = ConditionContext.forCard(cycledCard, cyclingPlayerId);
            if (targetingCyclingAbility == null || chooseOnTrigger) {
                put(gameData, ability, cycledCard, null, cyclingPlayerId, context, entry -> {
                    entry.setNonTargeting(true);
                    entry.setXValue(event.xValue());
                }, null);
                continue;
            }
            if (announcedTargets.isEmpty() && !targetingCyclingAbility.isCyclingTriggerWithoutTargets()
                    || !conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                continue;
            }
            boolean triggered = stackService.putWithAnnouncedTargets(gameData,
                    new TriggeredAbilityRequest(ability, cycledCard, null, cyclingPlayerId,
                            cycledCard.getName() + "'s ability", context, entry -> {
                                entry.setXValue(event.xValue());
                                if (announcedTargets.isEmpty()) {
                                    entry.setNonTargeting(true);
                                    return;
                                }
                                entry.chooseTargetIds(announcedTargets);
                                if (announcedTargets.size() == 1) {
                                    entry.setTargetId(announcedTargets.getFirst());
                                }
                                entry.setTargetFilter(targetingCyclingAbility.getTargetFilter());
                                entry.setMultiTargetConstraint(targetingCyclingAbility.getMultiTargetConstraint());
                            }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(cycledCard));
            }
        }
        boolean[] anyTriggered = {false};
        gameData.forEachPermanent((playerId, perm) -> {
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, PLAYER_CYCLES)) {
                requireNothingButLimit(read(ability, PLAYER_CYCLES), PLAYER_CYCLES);
                if (putDiscardAbility(gameData, ability, perm, playerId, cyclingPlayerId, List.of(cycledCard),
                        null)) {
                    anyTriggered[0] = true;
                }
            }
        });
        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * The "when you cycle this card" abilities of {@code cycledCard} as it has them in the zone it wound up in (CR
     * 702.29c): in a graveyard as the layers leave graveyard cards ({@link TriggerIndex#graveyardAbilities}: none while
     * cards in graveyards lose all abilities, Yixlid Jailer's ruling on Krosan Tusker), elsewhere (the exile or library
     * a replacement effect sent it to) its own and its perpetual grants.
     */
    private List<TriggeredAbility> cycledCardAbilities(GameData gameData, Card cycledCard) {
        boolean inGraveyard = gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> graveyard.stream().anyMatch(card -> card.getId().equals(cycledCard.getId())));
        return inGraveyard ? abilities.graveyardAbilities(gameData, cycledCard, YOU_CYCLE_THIS)
                : abilities.exileAbilities(gameData, cycledCard, YOU_CYCLE_THIS);
    }

    /**
     * A scry: the scrying player's "whenever you scry", then the other players' "whenever an opponent scries". The
     * number of cards put on the bottom is the event's value, which an event condition reads ("whenever you choose to
     * put one or more cards on the bottom of your library while scrying", The Temporal Anchor), with the scrying player
     * as "that player"; an opponent's scry names that player as the non-targeting "that player".
     */
    private void scried(GameData gameData, LibraryEvent.Scried event) {
        UUID scryingPlayerId = event.playerId();
        forEachPlayersPermanents(gameData, scryingPlayerId, (perm, controllerId, own) -> {
            LibraryTrigger watched = own ? YOU_SCRY : OPPONENT_SCRIES;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, watched)) {
                requireNothingButLimit(read(ability, watched), watched);
                ConditionContext context = ConditionContext.forPermanent(perm, controllerId)
                        .withTargetId(scryingPlayerId).withEventValue(event.bottomedCardCount());
                Permanent snapshot = new Permanent(perm);
                put(gameData, ability, perm.getCard(), perm, controllerId, context, entry -> {
                    entry.setNonTargeting(true);
                    entry.setSourcePermanentSnapshot(snapshot);
                    entry.setTriggeringPlayerId(scryingPlayerId);
                    entry.setEventValue(event.bottomedCardCount());
                    entry.setCardsLookedAtWhileScrying(event.cardsLookedAt());
                    if (!own) {
                        entry.setTargetId(scryingPlayerId);
                    }
                }, null);
            }
        });
    }

    /**
     * A surveil: the surveilling player's "whenever you surveil" ("for the first time each turn": their first surveil
     * this turn), the other players' "whenever an opponent surveils" (naming that player as the non-targeting "that
     * player"), then the "whenever you surveil" abilities of the cards in the surveilling player's graveyard.
     */
    private void surveilled(GameData gameData, LibraryEvent.Surveilled event) {
        UUID surveilingPlayerId = event.playerId();
        forEachPlayersPermanents(gameData, surveilingPlayerId, (perm, controllerId, own) -> {
            LibraryTrigger watched = own ? YOU_SURVEIL : OPPONENT_SURVEILS;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, watched)) {
                if (!surveilCountMatches(read(ability, watched), watched, event)) {
                    continue;
                }
                Permanent snapshot = new Permanent(perm);
                put(gameData, ability, perm.getCard(), perm, controllerId,
                        ConditionContext.forPermanent(perm, controllerId), entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                            entry.setTriggeringPlayerId(surveilingPlayerId);
                            if (!own) {
                                entry.setTargetId(surveilingPlayerId);
                            }
                        }, null);
            }
        });
        List<Card> graveyard = gameData.playerGraveyards.get(surveilingPlayerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : List.copyOf(graveyard)) {
            for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, YOU_SURVEIL)) {
                if (!surveilCountMatches(read(ability, YOU_SURVEIL), YOU_SURVEIL, event)) {
                    continue;
                }
                long stay = gameData.graveyardEntryVersion(card.getId());
                put(gameData, ability, card, null, surveilingPlayerId,
                        ConditionContext.forCard(card, surveilingPlayerId), entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourceGraveyardEntryVersion(stay);
                            entry.setTriggeringPlayerId(surveilingPlayerId);
                        }, null);
            }
        }
    }

    /** Whether {@code read}'s surveil ability triggers for {@code event}: every surveil, or the player's first this turn. */
    private static boolean surveilCountMatches(Read read, LibraryTrigger watched, LibraryEvent.Surveilled event) {
        requireNoFilter(read, watched);
        requireWatchedEvent(read, watched);
        if (read.counts().isEmpty()) {
            return true;
        }
        requireNoCountButFirst(read, watched);
        return event.firstThisTurn();
    }

    /**
     * A search of {@code searchingPlayerId}'s library: their permanents' "whenever you search your library" and the
     * other players' permanents' "whenever an opponent searches their library", the latter naming the searching player
     * as the non-targeting "that player".
     */
    private void searched(GameData gameData, UUID searchingPlayerId) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            boolean ownSearch = controllerId.equals(searchingPlayerId);
            LibraryTrigger watched = ownSearch ? YOU_SEARCH : OPPONENT_SEARCHES;
            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, perm, watched)) {
                    put(gameData, ability, perm.getCard(), perm, controllerId,
                            ConditionContext.forPermanent(perm, controllerId), entry -> {
                                entry.setNonTargeting(true);
                                if (!ownSearch) {
                                    entry.setTargetId(searchingPlayerId);
                                }
                            }, null);
                }
            }
        });
    }

    /**
     * A shuffle of a player's library: "whenever a player shuffles their library", an opponent's permanents' "whenever
     * an opponent shuffles their library", and, when a spell or ability the shuffling player controls made them
     * shuffle, "whenever a spell or ability causes its controller to shuffle their library". Each names the shuffling
     * player as the non-targeting "that player", with its source as it was. Nothing is logged: a library can be
     * shuffled outside a game action.
     */
    private void shuffled(GameData gameData, LibraryEvent.Shuffled event) {
        UUID shufflingPlayerId = event.playerId();
        boolean causedByOwnSpellOrAbility = shufflingPlayerId.equals(event.causingControllerId());
        gameData.forEachPermanent((controllerId, perm) -> {
            List<TriggeredAbility> watched = new ArrayList<>(abilities.abilities(gameData, perm, PLAYER_SHUFFLES));
            if (!controllerId.equals(shufflingPlayerId)) {
                watched.addAll(abilities.abilities(gameData, perm, OPPONENT_SHUFFLES));
            }
            if (causedByOwnSpellOrAbility) {
                watched.addAll(abilities.abilities(gameData, perm, SPELL_OR_ABILITY_CAUSES_SHUFFLE));
            }
            for (TriggeredAbility ability : watched) {
                ConditionContext context = ConditionContext.forPermanent(perm, controllerId);
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                Permanent snapshot = new Permanent(perm);
                stackService.put(gameData, new TriggeredAbilityRequest(ability, perm.getCard(), perm, controllerId,
                        perm.getCard().getName() + "'s ability", context, entry -> {
                            entry.setTargetId(shufflingPlayerId);
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                        }));
            }
        });
    }

    /**
     * Runs {@code action} for the permanents of {@code playerId}, then for each other player's, flagging which are the
     * player's own.
     */
    private static void forEachPlayersPermanents(GameData gameData, UUID playerId, PermanentAction action) {
        List<Permanent> own = gameData.playerBattlefields.get(playerId);
        if (own != null) {
            for (Permanent perm : List.copyOf(own)) {
                action.accept(perm, playerId, true);
            }
        }
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(playerId)) return;
            for (Permanent perm : List.copyOf(battlefield)) {
                action.accept(perm, controllerId, false);
            }
        });
    }

    @FunctionalInterface
    private interface PermanentAction {
        void accept(Permanent perm, UUID controllerId, boolean own);
    }

    /**
     * Hands {@code ability} of {@code sourceCard} ({@code source} on the battlefield, or {@code null} for a card in
     * another zone) to the stack door: its event condition is judged here in {@code context} (CR 603.2), and
     * {@code record} is stamped on its entry whatever its effects are; {@code cause} is the draw the "triggers an
     * additional time" effects of draws count, or {@code null}.
     *
     * @return whether it triggered
     */
    private boolean put(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                        UUID controllerId, ConditionContext context, TriggerRecord record, TriggerCause cause) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, sourceCard, source, controllerId,
                sourceCard.getName() + "'s ability", context, record);
        if (cause != null) {
            request = request.withCause(cause);
        }
        boolean triggered = stackService.put(gameData, request);
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

    /**
     * {@code ability}'s library conditions watched as {@code watched}, read as one: they must have the same event,
     * player and filter; only their counts may differ ("your first or second card each turn").
     *
     * @param ability the ability
     * @param filter  what the drawn, revealed or discarded card must be, or {@code null}
     * @param event   the conditions' own event, which may be narrower than the watched one
     * @param counts  which of the player's events this turn trigger it, or none for every one
     */
    private record Read(TriggeredAbility ability, CardPredicate filter, LibraryTrigger.LibraryEvent event,
                        Set<Integer> counts) {

        boolean countMatches(int count) {
            return counts.isEmpty() || counts.contains(count);
        }
    }

    /**
     * {@code ability} read for its conditions watched as {@code watched}. A library ability triggers once for each
     * card drawn or discarded and once for each other event, so it has no multiplicity, nor "once each turn for each
     * creature".
     */
    static Read read(TriggeredAbility ability, Trigger watched) {
        List<LibraryTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(LibraryTrigger.class::isInstance).map(LibraryTrigger.class::cast)
                .filter(library -> library.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no library condition watched as " + watched);
        }
        LibraryTrigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(library -> library.event() != first.event()
                || library.player() != first.player() || !Objects.equals(library.qualifier(), first.qualifier()))) {
            throw new IllegalStateException("The library conditions of one ability watched as " + watched
                    + " must read alike but for their counts: " + ability);
        }
        Set<Integer> counts = conditions.stream().map(LibraryTrigger::nth).filter(nth -> nth > 0)
                .collect(Collectors.toSet());
        if (!counts.isEmpty() && conditions.stream().anyMatch(trigger -> trigger.nth() == 0)) {
            throw new IllegalStateException("The library conditions of one ability watched as " + watched
                    + " must all count the player's events or none: " + ability);
        }
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No library matcher reads " + options.multiplicity() + ": " + ability);
        }
        if (options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No library matcher reads " + options.limit() + ": " + ability);
        }
        return new Read(ability, first.qualifier(), first.event(), counts);
    }

    private static void requireNothingToJudge(Read read, LibraryTrigger watched) {
        requireNothingButLimit(read, watched);
        if (read.ability().options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The matcher of " + watched + " judges no limit: " + read.ability());
        }
    }

    private static void requireNothingButLimit(Read read, LibraryTrigger watched) {
        requireNoFilter(read, watched);
        requireWatchedEvent(read, watched);
        requireNoCount(read, watched);
    }

    private static void requireNoFilter(Read read, LibraryTrigger watched) {
        if (read.filter() != null) {
            throw new IllegalStateException("The matcher of " + watched + " judges no filter: " + read.ability());
        }
    }

    private static void requireWatchedEvent(Read read, LibraryTrigger watched) {
        if (read.event() != watched.event()) {
            throw new IllegalStateException("The matcher of " + watched + " reads no " + read.event() + ": "
                    + read.ability());
        }
    }

    private static void requireNoCount(Read read, LibraryTrigger watched) {
        if (!read.counts().isEmpty()) {
            throw new IllegalStateException("The matcher of " + watched + " counts no events: " + read.ability());
        }
    }

    private static void requireNoCountButFirst(Read read, LibraryTrigger watched) {
        if (!read.counts().isEmpty() && !read.counts().equals(Set.of(1))) {
            throw new IllegalStateException("The matcher of " + watched + " counts only the first event of the "
                    + "turn: " + read.ability());
        }
    }
}
