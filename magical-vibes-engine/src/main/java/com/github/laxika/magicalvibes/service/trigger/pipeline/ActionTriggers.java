package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger.ObjectAction;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger.PlayerAction;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Triggers the player and keyword action abilities of an {@link ActionEvent} (CR 603.2): "whenever you roll one or
 * more dice", "whenever an opponent commits a crime", "whenever a creature you control explores", "when this creature
 * exploits a creature" and their kin, each handed whole to the stack door with the record of the event.
 *
 * <p>The abilities are read from {@link TriggerIndex} through {@link EventAbilitySupport} as the layers leave them (a
 * face-down permanent or one that lost its abilities has none of its card's, CR 708.2, 613.1f; its card's are read
 * through their text changes, CR 612.1; granted ones are its own) and matched by their own
 * {@link PlayerActionTrigger} or {@link ObjectActionTrigger}: the action, how the player or object relates to the
 * ability's controller and source, and the card the action is taken with against the trigger's filter. An ability's
 * event condition is judged here; its intervening "if" (CR 603.4) and limit (CR 603.2h) by the door.
 *
 * <p>A player's action triggers, in this order, the acting player's permanents' "whenever you …", the face-up schemes'
 * "whenever you set a scheme in motion", the other players' permanents' "whenever an opponent …", every permanent's
 * "whenever a player …", and then the abilities of the cards in graveyards that function there, related to their
 * owners the same way. The record names, whatever the effects are, the source as it was when the ability triggered
 * and what the event carries ({@link #eventRecord}); the door chooses the targets the card declares (CR 603.3d).
 */
@Slf4j
@Service
public class ActionTriggers {

    private final EventAbilitySupport abilities;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;

    public ActionTriggers(EventAbilitySupport abilities, @Lazy PredicateEvaluationService predicateEvaluationService,
                          @Lazy GameQueryService gameQueryService,
                          @Lazy TriggeredAbilityQueueService triggeredAbilityQueueService) {
        this.abilities = abilities;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, ActionEvent event) {
        switch (event) {
            case ActionEvent.Planeswalked ignored -> {
            }
            case ActionEvent.SchemeSetInMotion scheme -> playerAction(gameData, PlayerAction.SETS_SCHEME_IN_MOTION,
                    scheme.playerId(), Facts.of(entry -> {
                        entry.setTriggeringCardId(scheme.scheme().getCard().getId());
                        entry.setTriggeringSpell(scheme.scheme());
                    }).withActionCard(scheme.scheme().getCard()));
            case ActionEvent.GiftGiven gift -> playerAction(gameData, PlayerAction.GIVES_GIFT, gift.playerId(),
                    Facts.NONE);
            case ActionEvent.CaseSolved solved -> playerAction(gameData, PlayerAction.SOLVES_CASE, solved.playerId(),
                    Facts.of(entry -> {
                        entry.setTriggeringPermanentId(solved.solvedCase().getId());
                        entry.setTriggeringPermanentControllerId(solved.playerId());
                    }));
            case ActionEvent.Foretold foretold -> playerAction(gameData, PlayerAction.FORETELLS, foretold.playerId(),
                    Facts.of(entry -> {
                        entry.setTriggeringPlayerId(foretold.playerId());
                        entry.setTriggeringCardId(foretold.card().getId());
                    }));
            case ActionEvent.Foraged foraged -> playerAction(gameData, PlayerAction.FORAGES, foraged.playerId(),
                    Facts.NONE);
            case ActionEvent.VotingFinished voting -> playerAction(gameData, PlayerAction.FINISH_VOTING, null,
                    Facts.of(entry -> entry.setTriggeringVotingResult(voting.result())));
            case ActionEvent.RingBearerChosen chosen -> playerAction(gameData, PlayerAction.CHOOSES_RING_BEARER,
                    chosen.playerId(), Facts.NONE.judging((source, controllerId) -> source != null
                            && !source.getId().equals(chosen.ringBearerId())));
            case ActionEvent.TemptedByRing tempted -> playerAction(gameData, PlayerAction.TEMPTED_BY_RING,
                    tempted.playerId(), Facts.NONE);
            case ActionEvent.Investigated investigated -> {
                Facts facts = Facts.of(entry -> entry.setTriggeringPlayerId(investigated.playerId()));
                if (investigated.firstThisTurn()) {
                    playerAction(gameData, PlayerAction.INVESTIGATES_FIRST_TIME_EACH_TURN, investigated.playerId(),
                            facts);
                }
                playerAction(gameData, PlayerAction.INVESTIGATES, investigated.playerId(), facts);
            }
            case ActionEvent.DiceRolled dice -> {
                playerAction(gameData, PlayerAction.ROLLS_ONE_OR_MORE_DICE, dice.playerId(),
                        Facts.valued(dice.result()));
                if (dice.planar()) {
                    planarDieRolled(gameData, dice.playerId(), dice.result());
                }
            }
            case ActionEvent.DieRolled die -> {
                playerAction(gameData, PlayerAction.ROLLS_DIE, die.playerId(), Facts.valued(die.result()));
                if (die.result() == die.sides()) {
                    playerAction(gameData, PlayerAction.ROLLS_HIGHEST_NATURAL_RESULT, die.playerId(),
                            Facts.valued(die.result()));
                }
            }
            case ActionEvent.Natural20Rolled rolled -> playerAction(gameData, PlayerAction.ROLLS_NATURAL_20,
                    rolled.playerId(), Facts.NONE);
            case ActionEvent.ManifestedDread manifested -> {
                Card graveyardCard = manifested.cardPutIntoGraveyard();
                long stay = graveyardCard == null ? 0L : gameData.graveyardEntryVersion(graveyardCard.getId());
                playerAction(gameData, PlayerAction.MANIFESTS_DREAD, manifested.playerId(), Facts.of(entry -> {
                    if (graveyardCard != null) {
                        entry.setTriggeringCard(graveyardCard.getId(), stay);
                    }
                }));
            }
            case ActionEvent.DungeonCompleted completed -> playerAction(gameData, PlayerAction.COMPLETES_DUNGEON,
                    completed.playerId(), Facts.NONE);
            case ActionEvent.Bent bent -> playerAction(gameData, PlayerAction.BENDS, bent.playerId(), Facts.NONE);
            case ActionEvent.Exerted exerted -> playerAction(gameData, PlayerAction.EXERTS, exerted.playerId(),
                    Facts.of(entry -> entry.setTargetId(exerted.creatureId())));
            case ActionEvent.EvidenceCollected collected -> playerAction(gameData, PlayerAction.COLLECTS_EVIDENCE,
                    collected.playerId(), Facts.of(entry -> entry.setTriggeringPlayerId(collected.playerId())));
            case ActionEvent.Discovered discovered -> playerAction(gameData, PlayerAction.DISCOVERS,
                    discovered.playerId(), Facts.of(entry -> entry.setEventValue(discovered.value())));
            case ActionEvent.Sought sought -> playerAction(gameData, PlayerAction.SEEKS, sought.playerId(),
                    Facts.of(entry -> {
                        entry.setTriggeringPlayerId(sought.playerId());
                        entry.setTriggeringCards(sought.cards());
                    }));
            case ActionEvent.Conjured conjured -> playerAction(gameData, PlayerAction.CONJURES, conjured.playerId(),
                    Facts.of(entry -> entry.setEventValue(conjured.count())));
            case ActionEvent.BecameMonarch monarch -> {
                UUID monarchId = monarch.playerId();
                playerAction(gameData, PlayerAction.BECOMES_MONARCH, monarchId,
                        Facts.of(entry -> entry.setTriggeringPlayerId(monarchId)).namingThatPlayer());
                triggeredAbilityQueueService.beginStackDoorChoices(gameData);
            }
            case ActionEvent.CoinFlipWon won -> playerAction(gameData, PlayerAction.WINS_COIN_FLIP, won.playerId(),
                    Facts.NONE);
            case ActionEvent.CoinFlipLost lost -> playerAction(gameData, PlayerAction.LOSES_COIN_FLIP,
                    lost.playerId(), Facts.NONE);
            case ActionEvent.TaxPaid tax -> playerAction(gameData, PlayerAction.PAYS_TAX, tax.payingPlayerId(),
                    Facts.of(entry -> {
                        entry.setTriggeringPlayerId(tax.payingPlayerId());
                        entry.setTargetId(tax.payingPlayerId());
                        entry.setEventValue(tax.manaPaid());
                    }).judging((source, controllerId) -> controllerId.equals(tax.taxingPlayerId())));
            case ActionEvent.Clashed clashed -> {
                int outcome = clashed.won() ? 1 : 0;
                playerAction(gameData, PlayerAction.CLASHES, clashed.playerId(), Facts.of(entry -> {
                    entry.setTriggeringPlayerId(clashed.playerId());
                    entry.setEventValue(outcome);
                }).withContextValue(outcome));
                triggeredAbilityQueueService.beginStackDoorChoices(gameData);
            }
            case ActionEvent.LostGame lost -> playerLosesGame(gameData, lost.playerId());
            case ActionEvent.CrimeCommitted crime -> playerAction(gameData, PlayerAction.COMMITS_CRIME,
                    crime.playerId(), Facts.NONE);
            case ActionEvent.Sacrificed sacrificed -> playerAction(gameData,
                    PlayerAction.SACRIFICES_ONE_OR_MORE_PERMANENTS, sacrificed.playerId(), Facts.NONE);
            case ActionEvent.Fought fought -> fought(gameData, fought);
            case ActionEvent.Explored explored -> explored(gameData, explored);
            case ActionEvent.Connived connived -> connived(gameData, connived);
            case ActionEvent.Exploited exploited -> exploited(gameData, exploited);
            case ActionEvent.Championed championed -> championed(gameData, championed);
            case ActionEvent.Plotted plotted -> plotted(gameData, plotted);
            case ActionEvent.Mutated mutated -> mutated(gameData, mutated);
            case ActionEvent.ExertedAsItAttacks exerted -> {
                Facts facts = Facts.of(entry -> entry.setTargetId(exerted.creature().getId()));
                putObjectAbilities(gameData, exerted.creature(), exerted.playerId(),
                        objectWatched(ObjectAction.EXERTED_AS_IT_ATTACKS, ObjectRelation.THIS), facts);
            }
        }
    }

    /**
     * Hands each ability that watches {@code action} taken by {@code actorId} ({@code null} for an action of all the
     * players, which only "whenever a player …" watches) to the stack door, in the order the class says.
     */
    private void playerAction(GameData gameData, PlayerAction action, UUID actorId, Facts facts) {
        PlayerActionTrigger you = new PlayerActionTrigger(action, PlayerRelation.YOU);
        PlayerActionTrigger opponent = new PlayerActionTrigger(action, PlayerRelation.OPPONENT);
        PlayerActionTrigger any = new PlayerActionTrigger(action, PlayerRelation.ANY);
        if (actorId != null) {
            putBattlefieldAbilities(gameData, actorId, you, actorId, facts);
            if (action == PlayerAction.SETS_SCHEME_IN_MOTION) {
                gameData.forEachFaceUpSchemeSource((ownerId, scheme) -> {
                    if (ownerId.equals(actorId)) {
                        putPermanentAbilities(gameData, scheme, ownerId, you, actorId, facts);
                    }
                });
            }
            for (UUID playerId : List.copyOf(gameData.orderedPlayerIds)) {
                if (!playerId.equals(actorId)) {
                    putBattlefieldAbilities(gameData, playerId, opponent, actorId, facts);
                }
            }
        }
        gameData.forEachBattlefield((playerId, battlefield) ->
                putBattlefieldAbilities(gameData, playerId, any, actorId, facts));
        for (Watcher delayed : abilities.delayedWatchers(gameData)) {
            if (actorId != null) {
                putDelayedAbilities(gameData, delayed, delayed.controllerId().equals(actorId) ? you : opponent,
                        actorId, facts);
            }
            putDelayedAbilities(gameData, delayed, any, actorId, facts);
        }
        if (actorId != null) {
            putGraveyardAbilities(gameData, actorId, you, actorId, facts);
            for (UUID playerId : List.copyOf(gameData.orderedPlayerIds)) {
                if (!playerId.equals(actorId)) {
                    putGraveyardAbilities(gameData, playerId, opponent, actorId, facts);
                }
            }
        }
        for (UUID playerId : List.copyOf(gameData.orderedPlayerIds)) {
            putGraveyardAbilities(gameData, playerId, any, actorId, facts);
        }
    }

    private void putBattlefieldAbilities(GameData gameData, UUID controllerId, PlayerActionTrigger watched,
                                         UUID actorId, Facts facts) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : List.copyOf(battlefield)) {
            putPermanentAbilities(gameData, permanent, controllerId, watched, actorId, facts);
        }
    }

    private void putPermanentAbilities(GameData gameData, Permanent permanent, UUID controllerId,
                                       PlayerActionTrigger watched, UUID actorId, Facts facts) {
        for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
            if (matches(gameData, ability, watched, permanent.getCard(), permanent, controllerId, facts)) {
                putAbility(gameData, ability, permanent.getCard(), permanent, controllerId, actorId, watched,
                        facts);
            }
        }
    }

    /**
     * The abilities of the delayed triggered ability {@code delayed} ("you get a two-time boon with 'whenever you
     * sacrifice one or more permanents, ...'") that watch {@code action}, its "you" its controller (CR 603.7d, 603.7e).
     */
    private void putDelayedAbilities(GameData gameData, Watcher delayed, PlayerActionTrigger watched, UUID actorId,
                                     Facts facts) {
        UUID controllerId = delayed.controllerId();
        for (TriggeredAbility ability : abilities.abilities(gameData, delayed, watched)) {
            if (matches(gameData, ability, watched, delayed.card(), null, controllerId, facts)) {
                TriggerRecord event = eventRecord(watched, controllerId, actorId, facts);
                abilities.put(gameData, ability, delayed,
                        context(ConditionContext.forCard(delayed.card(), controllerId), facts), entry -> {
                            entry.setNonTargeting(true);
                            event.stamp(entry);
                        });
            }
        }
    }

    private void putGraveyardAbilities(GameData gameData, UUID ownerId, PlayerActionTrigger watched, UUID actorId,
                                       Facts facts) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : List.copyOf(graveyard)) {
            for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, watched)) {
                if (!matches(gameData, ability, watched, card, null, ownerId, facts)) {
                    continue;
                }
                long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
                abilities.put(gameData, ability, card, null, ownerId, context(ConditionContext.forCard(card, ownerId),
                        facts), entry -> {
                    entry.setNonTargeting(true);
                    entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
                    eventRecord(watched, ownerId, actorId, facts).stamp(entry);
                });
            }
        }
    }

    /**
     * Hands {@code ability} of {@code source} to the stack door, its event condition judged in its source's context
     * with the event's value, the record naming the source as it was and the event ({@link #eventRecord}).
     */
    private void putAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                            UUID controllerId, UUID actorId, Trigger watched, Facts facts) {
        Permanent snapshot = source == null ? null : new Permanent(source);
        TriggerRecord event = eventRecord(watched, controllerId, actorId, facts);
        ConditionContext context = source != null ? ConditionContext.forPermanent(source, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId);
        abilities.put(gameData, ability, sourceCard, source, controllerId, context(context, facts), entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            event.stamp(entry);
        });
    }

    /**
     * What the record stamps of the event: the facts' own record and, for "whenever an opponent becomes the monarch",
     * that player as the non-targeting "that player" (CR 115.10a).
     */
    private static TriggerRecord eventRecord(Trigger watched, UUID controllerId, UUID actorId, Facts facts) {
        boolean thatPlayer = facts.namesThatPlayer() && actorId != null && !actorId.equals(controllerId);
        return entry -> {
            facts.record().stamp(entry);
            if (thatPlayer) {
                entry.setTargetId(actorId);
            }
        };
    }

    private static ConditionContext context(ConditionContext context, Facts facts) {
        return facts.contextValue() == null ? context : context.withEventValue(facts.contextValue());
    }

    /**
     * Whether {@code ability}'s trigger watched as {@code watched} matches the event: the card the action is taken with
     * against the trigger's filter (the scheme set in motion, the explored card, the exploited creature), and what the
     * event itself judges of the source.
     */
    private boolean matches(GameData gameData, TriggeredAbility ability, Trigger watched, Card sourceCard,
                            Permanent source, UUID controllerId, Facts facts) {
        CardPredicate filter = filterOf(ability, watched);
        if (filter != null && (facts.actionCard() == null || !predicateEvaluationService.matchesCardPredicate(
                facts.actionCard(), filter, sourceCard.getId(), gameData, controllerId))) {
            return false;
        }
        return facts.sourceJudge() == null || facts.sourceJudge().test(source, controllerId);
    }

    /** The filter of {@code ability}'s action condition watched as {@code watched}, or {@code null}. */
    private static CardPredicate filterOf(TriggeredAbility ability, Trigger watched) {
        if (watched instanceof PlayerActionTrigger) {
            PlayerActionTrigger condition = EventAbilitySupport.condition(ability, PlayerActionTrigger.class, watched);
            return condition == null ? null : condition.filter();
        }
        ObjectActionTrigger condition = EventAbilitySupport.condition(ability, ObjectActionTrigger.class, watched);
        return condition == null ? null : condition.filter();
    }

    private static ObjectActionTrigger objectWatched(ObjectAction action, ObjectRelation relation) {
        return new ObjectActionTrigger(action, relation);
    }

    /** Hands each ability of {@code permanent} (as the layers leave it) watched as {@code watched} to the door. */
    private void putObjectAbilities(GameData gameData, Permanent permanent, UUID controllerId, Trigger watched,
                                    Facts facts) {
        for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
            if (matches(gameData, ability, watched, permanent.getCard(), permanent, controllerId, facts)) {
                putAbility(gameData, ability, permanent.getCard(), permanent, controllerId, null, watched, facts);
            }
        }
    }

    /**
     * Hands each printed ability of {@code card} watched as {@code watched} to the door, with {@code permanent} (on
     * the battlefield, or as it last existed there) as its source: the cards of a mutated permanent, an exploiting
     * creature that sacrificed itself.
     */
    private void putPrintedAbilities(GameData gameData, Card card, Permanent permanent, UUID controllerId,
                                     Trigger watched, Facts facts) {
        for (TriggeredAbility ability : card.getTriggeredAbilities()) {
            if (ability.hasWatchedTrigger(watched, Zone.BATTLEFIELD)
                    && matches(gameData, ability, watched, card, permanent, controllerId, facts)) {
                putAbility(gameData, ability, card, permanent, controllerId, null, watched, facts);
            }
        }
    }

    /**
     * "Whenever you roll one or more dice" abilities of the face-up planar objects, when the planar controller rolled
     * the planar die: the planar controller controls them (CR 901.6), the planar object as it was is their source, the
     * result their event value.
     */
    private void planarDieRolled(GameData gameData, UUID rollingPlayerId, int result) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(rollingPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        PlayerActionTrigger watched = new PlayerActionTrigger(PlayerAction.ROLLS_ONE_OR_MORE_DICE, PlayerRelation.YOU);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            PlanarObject planarSource = object.copy();
            for (TriggeredAbility ability : EventAbilitySupport.planarAbilities(object.getCard(), watched)) {
                abilities.put(gameData, ability, object.getCard(), null, planarControllerId,
                        ConditionContext.forCard(object.getCard(), planarControllerId).withEventValue(result),
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(result);
                            entry.setSourcePlanarObject(planarSource);
                        });
            }
        }
    }

    /**
     * "Whenever a player loses the game" abilities (CR 603.2): "that player" is the player who lost, the non-targeting
     * player the entry records and the intervening "if" reads. This engine is two-player and the game ends as a player
     * loses, so these go on the stack but don't resolve; a simulation doesn't put them there.
     */
    private void playerLosesGame(GameData gameData, UUID losingPlayerId) {
        PlayerActionTrigger watched = new PlayerActionTrigger(PlayerAction.LOSES_GAME, PlayerRelation.ANY);
        gameData.forEachPermanent((controllerId, permanent) -> {
            for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
                Card card = permanent.getCard();
                abilities.put(gameData, ability, card, permanent, controllerId,
                        ConditionContext.forPermanent(permanent, controllerId).withTargetId(losingPlayerId), entry -> {
                            entry.setTargetId(losingPlayerId);
                            entry.setNonTargeting(true);
                        }, null, card.getName() + "'s triggered ability",
                        GameLog.cardThen(card, "'s triggered ability goes on the stack ("
                                + gameData.playerIdToName.get(losingPlayerId) + " loses the game)."));
            }
        });
    }

    /**
     * "Whenever a creature you control fights": the fighting creature's controller's other permanents', then the
     * fighting creature's own, which triggers even if it died in the fight; the fighting creature is the triggering
     * permanent.
     */
    private void fought(GameData gameData, ActionEvent.Fought event) {
        Permanent fighter = event.creature();
        UUID controllerId = event.controllerId();
        Trigger watched = objectWatched(ObjectAction.FIGHTS, ObjectRelation.YOU_CONTROL);
        Facts facts = Facts.of(entry -> entry.setTriggeringPermanentId(fighter.getId()));
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent watcher : new ArrayList<>(battlefield)) {
                if (!watcher.getId().equals(fighter.getId())) {
                    putObjectAbilities(gameData, watcher, controllerId, watched, facts);
                }
            }
        }
        putObjectAbilities(gameData, fighter, controllerId, watched, facts);
    }

    /** "Whenever a creature you control explores [a land card]": the explored card judged against the filter. */
    private void explored(GameData gameData, ActionEvent.Explored event) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(event.controllerId());
        if (battlefield == null) {
            return;
        }
        Trigger watched = objectWatched(ObjectAction.EXPLORES, ObjectRelation.YOU_CONTROL);
        Facts facts = Facts.NONE.withActionCard(event.exploredCard());
        for (Permanent permanent : List.copyOf(battlefield)) {
            putObjectAbilities(gameData, permanent, event.controllerId(), watched, facts);
        }
    }

    /**
     * "Whenever a creature you control connives": the conniving creature, a creature controlled by its controller now
     * or, for one that left the battlefield, its last-known controller (CR 701.50b), is the triggering permanent and
     * the non-targeting "that creature".
     */
    private void connived(GameData gameData, ActionEvent.Connived event) {
        Permanent creature = event.creature();
        UUID controllerId = event.controllerId();
        if (controllerId == null) {
            if (!gameQueryService.isCreature(gameData, creature)) {
                return;
            }
            controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
            if (controllerId == null) {
                return;
            }
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        Trigger watched = objectWatched(ObjectAction.CONNIVES, ObjectRelation.YOU_CONTROL);
        Facts facts = Facts.of(entry -> {
            entry.setTargetId(creature.getId());
            entry.setTriggeringPermanentId(creature.getId());
        });
        for (Permanent watcher : List.copyOf(battlefield)) {
            putObjectAbilities(gameData, watcher, controllerId, watched, facts);
        }
    }

    /**
     * An exploit (CR 702.110a): first the exploiting permanent's "when this exploits a creature" abilities, its source
     * as it last existed when it sacrificed itself, with the sacrificed creature recorded ("the sacrificed creature's
     * power"; its targets, a spell's among them, chosen as it goes on the stack, CR 603.3d); then the exploiting
     * player's "whenever a creature you control exploits a [nontoken] creature", the exploiting permanent's own read as
     * it last existed when it isn't on the battlefield any more, the exploited creature's power as the event value.
     */
    private void exploited(GameData gameData, ActionEvent.Exploited event) {
        Card sourceCard = event.sourceCard();
        if (sourceCard != null) {
            exploitedBySource(gameData, event);
        }
        Permanent exploiter = event.exploiter();
        Card exploitedCard = event.exploitedCard();
        if (exploiter == null || exploitedCard == null) {
            return;
        }
        UUID playerId = event.exploitingPlayerId();
        Trigger exploits = objectWatched(ObjectAction.EXPLOITS, ObjectRelation.YOU_CONTROL);
        Trigger exploitsNontoken = objectWatched(ObjectAction.EXPLOITS_NONTOKEN_CREATURE, ObjectRelation.YOU_CONTROL);
        Facts facts = Facts.of(entry -> entry.setEventValue(event.exploitedPower())).withActionCard(exploitedCard);
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        boolean exploiterStillPresent = false;
        if (battlefield != null) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (permanent.getId().equals(exploiter.getId())) {
                    exploiterStillPresent = true;
                }
                putObjectAbilities(gameData, permanent, playerId, exploits, facts);
                if (!exploitedCard.isToken()) {
                    putObjectAbilities(gameData, permanent, playerId, exploitsNontoken, facts);
                }
            }
        }
        if (!exploiterStillPresent) {
            putPrintedAbilities(gameData, exploiter.getCard(), exploiter, playerId, exploits, facts);
            if (!exploitedCard.isToken()) {
                putPrintedAbilities(gameData, exploiter.getCard(), exploiter, playerId, exploitsNontoken, facts);
            }
        }
    }

    private void exploitedBySource(GameData gameData, ActionEvent.Exploited event) {
        Card sourceCard = event.sourceCard();
        UUID controllerId = event.sourceControllerId();
        UUID sourcePermanentId = event.sourcePermanentId();
        Permanent live = sourcePermanentId == null ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        Permanent source = live;
        if (source == null && sourcePermanentId != null) {
            source = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(sourcePermanentId)).findFirst().orElse(null);
        }
        StackEntry originalEntry = gameData.pendingEffectResolutionEntry;
        Trigger watched = objectWatched(ObjectAction.EXPLOITS, ObjectRelation.THIS);
        List<TriggeredAbility> exploitAbilities = live != null ? abilities.abilities(gameData, live, watched)
                : sourceCard.getTriggeredAbilities().stream()
                .filter(ability -> ability.hasWatchedTrigger(watched, Zone.BATTLEFIELD)).toList();
        for (TriggeredAbility ability : exploitAbilities) {
            Permanent snapshot = source == null ? null : new Permanent(source);
            ConditionContext context = source != null ? ConditionContext.forPermanent(source, controllerId)
                    : ConditionContext.forCard(sourceCard, controllerId);
            boolean triggered = abilities.put(gameData, ability, sourceCard, source, controllerId, context, entry -> {
                entry.setNonTargeting(true);
                if (snapshot != null) {
                    entry.setSourcePermanentSnapshot(snapshot);
                }
                if (originalEntry != null) {
                    entry.setSacrificedPermanentSnapshot(originalEntry.getSacrificedPermanentSnapshot());
                    entry.setSacrificedPower(originalEntry.getSacrificedPower());
                    entry.setSacrificedColorCount(originalEntry.getSacrificedColorCount());
                    entry.setSacrificedToughness(originalEntry.getSacrificedToughness());
                }
            });
            if (triggered) {
                log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    /**
     * "When a creature is championed with this permanent" (Mistbind Clique): each ability goes on the stack whole (CR
     * 603.3), its target chosen as it goes there (CR 603.3d), with the active player as the ability triggered.
     */
    private void championed(GameData gameData, ActionEvent.Championed event) {
        Permanent source = event.source();
        UUID controllerId = event.controllerId();
        UUID activePlayerId = gameData.activePlayerId;
        Trigger watched = objectWatched(ObjectAction.CHAMPIONS, ObjectRelation.THIS);
        for (TriggeredAbility ability : abilities.abilities(gameData, source, watched)) {
            abilities.put(gameData, ability, source.getCard(), source, controllerId,
                    ConditionContext.forPermanent(source, controllerId), entry -> {
                        entry.setNonTargeting(true);
                        entry.setActivePlayerId(activePlayerId);
                    });
        }
    }

    /** "When this card becomes plotted": the abilities of the plotted card in exile, where they function. */
    private void plotted(GameData gameData, ActionEvent.Plotted event) {
        Card card = event.card();
        Trigger watched = objectWatched(ObjectAction.BECOMES_PLOTTED, ObjectRelation.THIS);
        for (TriggeredAbility ability : abilities.exileAbilities(gameData, card, watched)) {
            abilities.put(gameData, ability, card, null, event.playerId(),
                    ConditionContext.forCard(card, event.playerId()), TriggerRecord.NONE);
        }
    }

    /**
     * A mutation (CR 702.140): each "whenever this creature mutates" ability of each card whose abilities trigger, then
     * their "whenever a creature you control mutates", then the controller's other permanents' "whenever a creature
     * you control mutates", the mutated permanent the triggering permanent and the non-targeting "that creature".
     */
    private void mutated(GameData gameData, ActionEvent.Mutated event) {
        Permanent mutated = event.permanent();
        UUID controllerId = event.controllerId();
        Trigger self = objectWatched(ObjectAction.MUTATES, ObjectRelation.THIS);
        Trigger ally = objectWatched(ObjectAction.MUTATES, ObjectRelation.YOU_CONTROL);
        for (Card card : event.mutatedCards()) {
            putPrintedAbilities(gameData, card, mutated, controllerId, self, Facts.NONE);
        }
        Facts facts = Facts.of(entry -> {
            entry.setTargetId(mutated.getId());
            entry.setTriggeringPermanentId(mutated.getId());
        });
        for (Card card : event.mutatedCards()) {
            putPrintedAbilities(gameData, card, mutated, controllerId, ally, facts);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent watcher : List.copyOf(battlefield)) {
                if (!watcher.getId().equals(mutated.getId())) {
                    putObjectAbilities(gameData, watcher, controllerId, ally, facts);
                }
            }
        }
    }

    /**
     * What an action event carries for the abilities it triggers.
     *
     * @param record          what the entry records of the event
     * @param actionCard      the card the action is taken with, which a trigger's filter judges, or {@code null}
     * @param contextValue    the event's value an event condition reads, or {@code null}
     * @param namesThatPlayer whether an opponent's action names that player as the non-targeting "that player"
     * @param sourceJudge     what the event judges of an ability's source and controller, or {@code null}
     */
    private record Facts(TriggerRecord record, Card actionCard, Integer contextValue, boolean namesThatPlayer,
                         BiPredicate<Permanent, UUID> sourceJudge) {

        static final Facts NONE = of(TriggerRecord.NONE);

        static Facts of(TriggerRecord record) {
            return new Facts(record, null, null, false, null);
        }

        /** The event's value both recorded and read by an event condition: a die's result. */
        static Facts valued(int value) {
            return new Facts(entry -> entry.setEventValue(value), null, value, false, null);
        }

        Facts withActionCard(Card card) {
            return new Facts(record, card, contextValue, namesThatPlayer, sourceJudge);
        }

        Facts withContextValue(int value) {
            return new Facts(record, actionCard, value, namesThatPlayer, sourceJudge);
        }

        Facts namingThatPlayer() {
            return new Facts(record, actionCard, contextValue, true, sourceJudge);
        }

        Facts judging(BiPredicate<Permanent, UUID> judge) {
            return new Facts(record, actionCard, contextValue, namesThatPlayer, judge);
        }
    }
}
