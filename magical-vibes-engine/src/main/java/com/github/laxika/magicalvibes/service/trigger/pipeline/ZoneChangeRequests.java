package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.trigger.CreatureDeath;

/**
 * The requests the stack door's zone-change and permanent-change routes hand themselves
 * ({@link TriggeredAbilityStackService#triggerOnce}, {@link TriggeredAbilityStackService#triggerPermanentChange}): for an
 * ability that watches another object move, phase, transform or be turned face up, what its trigger adds to the
 * pipeline's match (the commander, "for the first time each turn"), its record of the event whatever its effects are,
 * its "that player" by the trigger's relation, and the cause the "triggers an additional time" effects judge.
 */
@Component
@RequiredArgsConstructor
public class ZoneChangeRequests {

    private final GameQueryService gameQueryService;

    /**
     * The request {@code candidate}'s "whenever [another object] …" ability, whose trigger condition {@code trigger}
     * {@code events} met, hands the stack door for those zone changes of one batch: one for an ability that triggers
     * for each, all of them for "one or more" (CR 603.2c). The trigger pipeline has already judged its relation, its
     * filter, the zone it functions from and the look-back of the event (CR 603.10a); this judges what the trigger
     * adds ({@link #zoneChangeEventMatches}) and records the event whatever the effects are ({@link #zoneChangeRecord}).
     * {@code null} when the event isn't the ability's.
     */
    public TriggeredAbilityRequest zoneChangeRequest(GameData gameData, TriggerCandidate candidate,
                                                     ZoneChangeTrigger trigger, List<ZoneChangeEvent> events,
                                                     ConditionContext ifContext) {
        if (!zoneChangeEventMatches(gameData, trigger, candidate, events)) {
            return null;
        }
        ZoneChangeEvent event = events.getFirst();
        Card sourceCard = candidate.sourceCard();
        Permanent source = candidate.sourcePermanent() == null ? null : namedSourcePermanent(candidate);
        boolean dies = trigger.to() == Zone.GRAVEYARD && trigger.from().equals(Set.of(Zone.BATTLEFIELD));
        TriggerRecord eventRecord = zoneChangeRecord(gameData, candidate, source, events,
                thatPlayer(gameData, trigger, event),
                dies && trigger.causes().isEmpty()
                        && candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE,
                dies && trigger.relation() == ObjectRelation.ENCHANTED,
                trigger.from().equals(Set.of(Zone.BATTLEFIELD)) && trigger.relation() == ObjectRelation.ENCHANTED);
        // A plane's ability names the planar object it is an ability of, as it was when it triggered (CR 608.2h). An
        // emblem's targets are only those its own effects declare: none of its source card's groups is its.
        PlanarObject plane = candidate.plane() == null ? null : candidate.plane().copy();
        boolean emblem = candidate.emblem() != null;
        TriggerRecord record = plane == null && !emblem ? eventRecord : entry -> {
            if (emblem) {
                entry.setMadeByResolution(true);
            }
            eventRecord.stamp(entry);
            if (plane != null) {
                entry.setSourcePlanarObject(plane);
            }
        };
        if (candidate.delayed() != null) {
            // A delayed ability records what its maker gave it (CR 603.7c).
            record = EventAbilitySupport.delayedRecord(candidate.delayed(), record);
        }
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(watchedAbility(gameData, candidate), sourceCard,
                source, candidate.controllerId(),
                sourceCard.getName() + (emblem ? "'s emblem" : "'s ability"), ifContext, record);
        if (emblem) {
            // An emblem isn't a permanent (CR 110.1): only the effects that count an emblem's abilities apply to it.
            request = request.ofEmblem();
        }
        if (event.enteredBattlefield() && event.permanent() != null && !createsTokens(candidate)) {
            Permanent entered = gameQueryService.findPermanentById(gameData, event.permanent().getId());
            request = request.withCause(new TriggerCause.Entered(
                    entered != null ? entered.getCard() : event.permanent().getCard(), event.controllerId(),
                    event.permanent().getId()));
        }
        if (event.died() && event.permanent() != null && event.permanent().getCard().hasType(CardType.CREATURE)) {
            request = request.withCause(new TriggerCause.CreatureDied());
            if (!triggersOnSacrifice(candidate)) {
                request = request.withCreatureDying();
            }
        }
        return request;
    }

    /**
     * The request {@code candidate}'s ability, whose trigger condition {@code trigger} {@code events} met, hands the
     * stack door for those permanents phasing, transforming or being turned face up in one batch: the permanent that
     * changed as "it" and "that creature" (unless the ability is about its own source, CR 113.7), its controller, and
     * the X its own source was turned face up for.
     */
    public TriggeredAbilityRequest permanentChangeRequest(GameData gameData, TriggerCandidate candidate,
                                                          PermanentChangeTrigger trigger,
                                                          List<PermanentChangeEvent> events,
                                                          ConditionContext ifContext) {
        PermanentChangeEvent event = events.getFirst();
        Permanent changed = event.change() == PermanentChange.PHASED_OUT ? event.permanent()
                : Objects.requireNonNullElse(gameQueryService.findPermanentById(gameData, event.permanent().getId()),
                event.permanent());
        Card sourceCard = candidate.sourceCard();
        Permanent source = candidate.sourcePermanent() == null ? null : namedSourcePermanent(candidate);
        Permanent snapshot = source == null ? null : new Permanent(source);
        long graveyardStay = candidate.zone() == Zone.GRAVEYARD ? gameData.graveyardEntryVersion(sourceCard.getId())
                : -1;
        boolean ownChange = trigger.relation() == ObjectRelation.THIS;
        // "Untap that creature", "it": the permanent the event was about, unless it is the source itself.
        UUID thatPermanentId = ownChange ? null : changed.getId();
        boolean ownTurnFaceUp = ownChange && event.change() == PermanentChange.TURNED_FACE_UP;
        List<UUID> changedIds = events.stream().map(changeEvent -> changeEvent.permanent().getId()).toList();
        TriggerRecord record = entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (graveyardStay >= 0) {
                entry.setSourceGraveyardEntryVersion(graveyardStay);
            }
            entry.setTriggeringPermanentId(changed.getId());
            entry.setTriggeringPermanentIds(changedIds);
            entry.setTriggeringPermanentControllerId(event.controllerId());
            entry.rememberLastKnownPermanentCard(changed.getId(), changed.getCard());
            if (thatPermanentId != null) {
                entry.setTargetId(thatPermanentId);
            }
            if (ownTurnFaceUp) {
                entry.setXValue(event.xValue());
            }
        };
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(watchedAbility(gameData, candidate), sourceCard,
                source, candidate.controllerId(), sourceCard.getName() + "'s ability", ifContext, record);
        // A permanent being turned face up is the event the "triggers an additional time" effects that count only
        // that event judge.
        return event.change() == PermanentChange.TURNED_FACE_UP
                ? request.withCause(new TriggerCause.TurnedFaceUp())
                : request;
    }

    /**
     * {@code candidate}'s ability with its effects read through its source's text changes (layer 3, CR 612.1).
     */
    private TriggeredAbility watchedAbility(GameData gameData, TriggerCandidate candidate) {
        return candidate.sourcePermanent() == null ? candidate.ability()
                : textChanged(gameData, candidate.sourcePermanent(), candidate.ability());
    }

    /** Whether {@code candidate}'s ability triggers on tokens being created, not on permanents entering. */
    private static boolean createsTokens(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger
                        && trigger.causes().contains(ZoneChangeCause.CREATE));
    }

    /**
     * Whether {@code candidate}'s ability triggers on a sacrifice ("whenever you sacrifice a creature"): a creature
     * dying doesn't cause it, so "a creature dying causes an ability to trigger an additional time" effects don't
     * apply to it (Teysa Karlov's ruling).
     */
    private static boolean triggersOnSacrifice(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger move
                        && move.causes().equals(Set.of(ZoneChangeCause.SACRIFICE)));
    }

    /**
     * Whether the zone changes {@code events} are {@code candidate}'s ability's event beyond what the trigger pipeline
     * judged, as its trigger {@code trigger} reads: "for the first time each turn" counts the events of
     * its kind this turn (the tokens you created, Akim, the Soaring Wind; the land cards put into your graveyard,
     * Crawling Sensation): only an event that holds all of them triggers it. Entering permanents a static ability
     * keeps from triggering abilities (Elesh Norn, Mother of Machines) are the stack door's to judge.
     */
    private boolean zoneChangeEventMatches(GameData gameData, ZoneChangeTrigger trigger, TriggerCandidate candidate,
                                           List<ZoneChangeEvent> events) {
        UUID controllerId = candidate.controllerId();
        if (candidate.ability().options().limit() != TriggerLimit.FIRST_TIME_EACH_TURN) {
            return true;
        }
        if (trigger.causes().contains(ZoneChangeCause.CREATE)) {
            return gameData.tokensCreatedThisTurn.getOrDefault(controllerId, 0) <= events.size();
        }
        if (trigger.to() == Zone.GRAVEYARD && Triggers.LAND_CARD.equals(trigger.filter())) {
            return gameData.landCardGraveyardEntriesThisTurn.getOrDefault(controllerId, 0) <= events.size();
        }
        throw new IllegalStateException("No zone-change matcher counts \"for the first time each turn\" of "
                + trigger + ": " + candidate.ability());
    }

    /**
     * CR 400.7f for an Aura that left as the permanent it enchanted left: the stay it has in the graveyard it was put
     * into at the same time, none that can be found if it went elsewhere. {@code -1} for one still on the battlefield,
     * which only the state-based action (CR 704.5m) can still give one.
     */
    private long stayOfAuraThatLeftWithItsHost(GameData gameData, Permanent aura) {
        if (gameQueryService.findPermanentById(gameData, aura.getId()) != null) {
            return -1;
        }
        UUID cardId = aura.getOriginalCard().getId();
        return gameQueryService.findCardInGraveyardById(gameData, cardId) != null
                ? gameData.graveyardEntryVersion(cardId) : StackEntry.NO_GRAVEYARD_STAY;
    }

    /**
     * The player a zone-change ability's trigger {@code trigger} names as "that player", its non-targeting target
     * (CR 115.10a): the opponent or enchanted player who controlled the object that moved ("whenever a creature an
     * opponent controls dies", "a creature enchanted player controls enters"), the opponent whose card it was ("a card
     * is put into an opponent's graveyard"), the player who put a permanent onto the battlefield ("whenever a player
     * puts a nontoken creature onto the battlefield": the caster of the spell it resolved from, Nature's Wrath's
     * ruling), the player who sacrificed a permanent ("whenever a player sacrifices a creature"), and the player whose
     * hand a permanent was returned to; {@code null} for an ability whose trigger names none.
     */
    private UUID thatPlayer(GameData gameData, ZoneChangeTrigger trigger, ZoneChangeEvent event) {
        ObjectRelation relation = trigger.relation();
        boolean anyObject = relation == ObjectRelation.ANY || relation == ObjectRelation.ANOTHER;
        if (relation == ObjectRelation.OPPONENT_CONTROLS || relation == ObjectRelation.ENCHANTED_PLAYER_CONTROLS) {
            return event.controllerId();
        }
        if (relation == ObjectRelation.OPPONENT_OWNS) {
            return event.ownerId();
        }
        if (relation == ObjectRelation.ANY && trigger.entersBattlefield()) {
            Permanent entered = event.permanent() == null ? null
                    : gameQueryService.findPermanentById(gameData, event.permanent().getId());
            return entered != null && entered.getCastControllerId() != null ? entered.getCastControllerId()
                    : event.controllerId();
        }
        if (anyObject && trigger.causes().contains(ZoneChangeCause.SACRIFICE)) {
            return event.controllerId();
        }
        if (anyObject && trigger.to() == Zone.HAND) {
            return event.ownerId();
        }
        return null;
    }

    /**
     * The record of a zone-change ability's {@code events}, stamped on its entry whatever its effects are, with the
     * source as it was when the ability triggered (CR 608.2h) and the stay in the graveyard of a source card there
     * ("this card", CR 400.7):
     * <ul>
     *   <li>a permanent that left the battlefield: as it last existed (CR 603.10a) with what was attached to it, as the
     *       triggering permanent ("it", "its counters", "that creature"), its controller, power, toughness and mana
     *       value then, its power as the event value, and the card it left as with the graveyard or exile
     *       stay it went to ("that card");</li>
     *   <li>a permanent that entered: as "it", with its controller, power and toughness as it entered, the card it
     *       entered as and the player or planeswalker it entered attacking;</li>
     *   <li>a card that moved between other zones: as "that card" with its graveyard stay and its owner;</li>
     *   <li>for "one or more": every object of the batch ("them", "those cards") and how many there were as the event
     *       value, or, for creatures dying, their total power (The Skullspore Nexus);</li>
     *   <li>"that player" ({@code thatPlayerId}) as its triggering player and non-targeting target.</li>
     * </ul>
     */
    private TriggerRecord zoneChangeRecord(GameData gameData, TriggerCandidate candidate, Permanent source,
                                           List<ZoneChangeEvent> events, UUID thatPlayerId, boolean deathBatch,
                                           boolean enchantedPermanent, boolean enchantedLeft) {
        ZoneChangeEvent event = events.getFirst();
        Permanent snapshot = source == null ? null : new Permanent(source);
        long sourceGraveyardStay = candidate.zone() == Zone.GRAVEYARD
                ? gameData.graveyardEntryVersion(candidate.sourceCard().getId()) : -1;
        boolean oneOrMore = candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        List<Card> cards = events.stream().map(ZoneChangeEvent::card).toList();
        Map<UUID, Long> graveyardStays = new java.util.HashMap<>();
        Map<UUID, Long> exileStays = new java.util.HashMap<>();
        for (ZoneChangeEvent moved : events) {
            Card card = movedCard(moved);
            if (moved.to() == Zone.GRAVEYARD) {
                graveyardStays.put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
            } else if (oneOrMore && wentToExile(gameData, moved, card)) {
                exileStays.put(card.getId(), gameData.exileEntryVersions.getOrDefault(card.getId(), 0L));
            }
        }
        Card movedCard = movedCard(event);
        // The card's stay in exile when it went there, or a replacement effect exiled it instead (Leyline of the
        // Void); -1 when it didn't. One put into a graveyard and exiled from there within the same batch didn't: only
        // the first zone it went to counts (CR 603.6c).
        long exileStay = wentToExile(gameData, event, movedCard)
                ? gameData.exileEntryVersions.getOrDefault(movedCard.getId(), 0L) : -1L;
        Permanent lastKnown = event.permanent();
        Permanent entered = event.enteredBattlefield() && lastKnown != null
                ? gameQueryService.findPermanentById(gameData, lastKnown.getId()) : null;
        UUID enteredControllerId = entered == null ? event.controllerId()
                : Objects.requireNonNullElse(gameQueryService.findPermanentController(gameData, entered.getId()),
                event.controllerId());
        int enteredPower = entered == null ? 0 : gameQueryService.getEffectivePower(gameData, entered);
        int enteredToughness = entered == null ? 0 : gameQueryService.getEffectiveToughness(gameData, entered);
        UUID attackedTargetId = entered != null && entered.isAttacking() ? entered.getAttackTarget() : null;
        UUID defendingPlayerId = attackedTargetId == null ? null : gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId : gameQueryService.findPermanentController(gameData, attackedTargetId);
        int totalPower = 0;
        for (ZoneChangeEvent moved : events) {
            if (moved.leftBattlefield() && moved.permanent() != null) {
                totalPower = SaturatingMath.add(totalPower, CreatureDeath.of(moved, gameData, gameQueryService).power());
            }
        }
        int batchPower = totalPower;
        UUID cardOwnerId = event.ownerId() != null ? event.ownerId() : gameData.ownerOf(event.card());
        long auraStay = enchantedLeft && source != null ? stayOfAuraThatLeftWithItsHost(gameData, source) : -1;
        return entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (sourceGraveyardStay >= 0) {
                entry.setSourceGraveyardEntryVersion(sourceGraveyardStay);
            }
            if (thatPlayerId != null) {
                entry.setTargetId(thatPlayerId);
                entry.setTriggeringPlayerId(thatPlayerId);
            }
            if (event.leftBattlefield() && lastKnown != null) {
                CreatureDeath death = CreatureDeath.of(event, gameData, gameQueryService);
                entry.setTriggeringPermanentId(lastKnown.getId());
                entry.rememberLastKnownPermanentCard(lastKnown.getId(), lastKnown.getCard());
                entry.setTriggeringPermanent(lastKnown, event.attachments());
                entry.setTriggeringPermanentControllerId(event.controllerId());
                entry.setTriggeringPermanentPowerAtTrigger(death.power());
                entry.setTriggeringPermanentToughnessAtTrigger(death.toughness());
                entry.setDyingPermanentManaValue(lastKnown.getCard().getManaValue());
                // As it last existed, which can be negative (CR 107.1b), for a comparison ("the difference").
                entry.setEventValue(death.power());
                if (event.to() == Zone.EXILE) {
                    entry.setTriggeringCardId(movedCard.getId());
                }
                entry.setTriggeringCardExileEntryVersion(exileStay);
                entry.setTriggeringPermanentOwnerId(event.ownerId());
                if (enchantedPermanent) {
                    // "Return that card": every card the enchanted permanent put into the graveyard (a merged
                    // permanent's too), each with its graveyard stay (CR 400.7). The Aura is still on the battlefield
                    // as its ability triggers and goes to its owner's graveyard with the state-based actions that
                    // follow (CR 704.5m): that stay is the one "return this card" means.
                    List<Card> leaving = lastKnown.cardsLeavingBattlefield();
                    entry.setTriggeringCards(leaving);
                    for (Card card : leaving) {
                        entry.getEventCardGraveyardEntryVersions().put(card.getId(),
                                gameData.graveyardEntryVersion(card.getId()));
                    }
                }
                if (enchantedLeft) {
                    // "Return this Aura": also after "when enchanted creature leaves the battlefield" (CR 400.7f).
                    if (auraStay >= 0) {
                        entry.setSourceGraveyardEntryVersion(auraStay);
                    } else {
                        entry.setAwaitingSourceGraveyardEntry(true);
                    }
                }
            } else if (event.enteredBattlefield() && lastKnown != null) {
                entry.setTriggeringCardSnapshot(event.card());
                entry.setTriggeringPermanentControllerId(enteredControllerId);
                if (entered != null) {
                    entry.setTriggeringPermanentId(entered.getId());
                    entry.setTriggeringCardId(entered.getCard().getId());
                    entry.setTriggeringPermanentPowerAtTrigger(enteredPower);
                    entry.setTriggeringPermanentToughnessAtTrigger(enteredToughness);
                    entry.rememberLastKnownPermanentCard(entered.getId(), entered.getCard());
                    entry.setAttachedPermanentSnapshot(new Permanent(entered));
                    entry.setAttackedTargetId(attackedTargetId);
                    entry.setDefendingPlayerId(defendingPlayerId);
                }
                entry.setTriggeringPermanentIds(events.stream().map(moved -> moved.permanent().getId()).toList());
                for (ZoneChangeEvent moved : events) {
                    entry.rememberLastKnownPermanentCard(moved.permanent().getId(), moved.permanent().getCard());
                }
            } else {
                entry.setTriggeringCardId(movedCard.getId());
                entry.setTriggeringCardSnapshot(event.card());
                // "That card's owner": the zone change names it, as a card in a hand or library may name none.
                entry.setTriggeringPermanentOwnerId(cardOwnerId);
            }
            if (graveyardStays.containsKey(movedCard.getId())) {
                entry.setTriggeringCard(movedCard.getId(), graveyardStays.get(movedCard.getId()));
            }
            if (oneOrMore) {
                entry.setTriggeringCards(cards);
                entry.setEventCardIds(cards.stream().map(Card::getId).toList());
                entry.getEventCardGraveyardEntryVersions().putAll(graveyardStays);
                entry.getEventCardExileEntryVersions().putAll(exileStays);
                if (event.leftBattlefield()) {
                    entry.setTriggeringPermanentIds(events.stream().filter(moved -> moved.permanent() != null)
                            .map(moved -> moved.permanent().getId()).toList());
                }
                entry.setEventValue(deathBatch ? Math.max(0, batchPower) : events.size());
            }
        };
    }

    /** The card a zone change moved, as it is now: for a departure, the permanent's physical card. */
    /**
     * Whether {@code event} put {@code card} into exile, or a replacement effect exiled it instead (Leyline of the Void).
     * One put into a graveyard and exiled from there within the same batch didn't: only the first zone it went to counts
     * (CR 603.6c).
     */
    private boolean wentToExile(GameData gameData, ZoneChangeEvent event, Card card) {
        return event.to() == Zone.EXILE || event.to() != Zone.GRAVEYARD
                && gameQueryService.findCardInExileById(gameData, card.getId()) != null;
    }

    private static Card movedCard(ZoneChangeEvent event) {
        return event.leftBattlefield() && event.permanent() != null && event.permanent().getOriginalCard() != null
                ? event.permanent().getOriginalCard() : event.card();
    }

    /**
     * The source permanent an old collector body puts on the stack. A permanent that left in the same event is its
     * last-known copy, whose card is a layered copy; the body names the card the candidate names instead, as the
     * pipeline's own stack entries do, so effects that act on "this card" find it.
     */
    private static Permanent namedSourcePermanent(TriggerCandidate candidate) {
        Permanent perm = candidate.sourceAsPermanent();
        if (candidate.sourcePermanent() == null || perm.getCard() == candidate.sourceCard()) {
            return perm;
        }
        Permanent named = new Permanent(perm);
        named.setCard(candidate.sourceCard());
        return named;
    }

    /** {@code ability} of {@code perm} read through its text changes (Layer 3, CR 612.1). */
    private static TriggeredAbility textChanged(GameData gameData, Permanent perm, TriggeredAbility ability) {
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        if (gameData.textReplacements(perm.getId()).isEmpty() && globalWordChange.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = TextChangeTransformer.transform(registration.effect(), gameData.textReplacements(perm.getId()),
                    globalWordChange);
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        return changed ? ability.withEffects(transformed) : ability;
    }
}
