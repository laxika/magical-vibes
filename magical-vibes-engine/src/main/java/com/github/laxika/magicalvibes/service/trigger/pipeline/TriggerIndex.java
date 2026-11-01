package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lists the triggered abilities a rules event may trigger, as the layers leave them (CR 613.1f).
 *
 * <p>For now it lists only the abilities of the object that moved, the candidates of a {@code THIS} trigger:
 * <ul>
 *   <li>A permanent that left the battlefield triggers as it last existed there (CR 603.10a): the abilities of
 *       the effective card it left as (also the source the stack entry names), those granted to it until it
 *       leaves, and those other objects' static abilities granted it, all on the event's snapshot. A permanent
 *       that had lost all its abilities has none of those, and a face-down one has none of its card's. Abilities
 *       granted until end of turn count either way, as the slot collectors count them. Its delayed "when this
 *       dies" triggers aren't abilities of it, so they count either way too; until delayed triggers move they
 *       ride on the snapshot as effects, matched as "when this dies" abilities.</li>
 *   <li>Every moved card, a departed permanent's included, also triggers as it exists after the move, in the
 *       zone it went to (CR 603.10): its card's abilities and its perpetual grants. That is where "when this is
 *       put into a graveyard from anywhere" triggers from, even for a move from the battlefield (CR 603.6c).
 *       Cards in graveyards have none while an effect such as Yixlid Jailer's says so.</li>
 * </ul>
 * A candidate's zone says which of the two it is, so each trigger condition matches only the one it functions
 * from.
 *
 * <p>The other objects that may see a permanent die ({@link #observerCandidates}) are listed as they were just
 * before the event (CR 603.10a): the permanents on the battlefield, those that left in the same event as their
 * last-known copies, the cards in graveyards that were there before it, and the cards in exile that haunt the
 * permanent (CR 702.55c). Abilities their owner perpetually lost are left out everywhere.
 */
@Service
public class TriggerIndex {

    private final GameQueryService gameQueryService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final PredicateEvaluationService predicateEvaluationService;

    public TriggerIndex(@Lazy GameQueryService gameQueryService,
                        @Lazy GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                        @Lazy PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    /**
     * The triggered abilities of the objects other than the one that left the battlefield in {@code event} that may
     * see it leave, with the controller each ability has: only abilities with a zone-change trigger condition.
     * {@code batch} is the occurrence {@code event} is part of (CR 603.2c); the permanents that left in it see one
     * another leave, as they last existed (CR 603.10a).
     */
    public List<TriggerCandidate> observerCandidates(GameData gameData, ZoneChangeEvent event,
                                                     List<ZoneChangeEvent> batch) {
        List<TriggerCandidate> candidates = new ArrayList<>();
        if (!event.leftBattlefield() || event.permanent() == null) {
            return candidates;
        }
        UUID departedId = event.permanent().getId();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!permanent.getId().equals(departedId)) {
                battlefieldCandidates(gameData, event, batch, permanent, controllerId, candidates);
            }
        });
        for (ZoneChangeEvent other : batch) {
            if (other != event && other.leftBattlefield() && other.permanent() != null
                    && other.controllerId() != null && !other.permanent().getId().equals(departedId)
                    && gameQueryService.findPermanentById(gameData, other.permanent().getId()) == null) {
                lookBackCandidates(gameData, other, candidates);
            }
        }
        graveyardCandidates(gameData, batch, candidates);
        hauntingCandidates(gameData, event, candidates);
        return candidates;
    }

    private void battlefieldCandidates(GameData gameData, ZoneChangeEvent event, List<ZoneChangeEvent> batch,
                                       Permanent permanent, UUID controllerId, List<TriggerCandidate> into) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        boolean lostAll = permanent.isLosesAllAbilitiesUntilEndOfTurn() || staticBonus.losesAllAbilities()
                || staticBonus.losesAllNonManaAbilities();
        List<TriggeredAbility> abilities = new ArrayList<>();
        // An ability lost to an effect that ended with the departure was still lost as it left
        // (lostPrintedAbilityWatcherIdsBeforeLeave, CR 603.10a).
        if (!lostAll && !permanent.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, permanent)
                && !gameData.lostPrintedAbilityWatcherIdsBeforeLeave.contains(permanent.getId())) {
            abilities.addAll(withoutLost(gameData, permanent.getCard().getId(),
                    permanent.getCard().getTriggeredAbilities()));
        }
        if (!lostAll) {
            abilities.addAll(permanent.getPersistentTriggeredAbilities());
            abilities.addAll(grantedTriggeredAbilitySupport.grantedZoneChangeAbilities(gameData, permanent));
            abilities.addAll(lookBackConditionalGrants(gameData, batch, permanent, controllerId, abilities));
        }
        abilities.addAll(permanent.getTemporaryTriggeredAbilities());
        addZoneChangeCandidates(permanent.getCard(), permanent, controllerId, Zone.BATTLEFIELD, abilities, into);
    }

    private void lookBackCandidates(GameData gameData, ZoneChangeEvent departure, List<TriggerCandidate> into) {
        Permanent lastKnown = departure.permanent();
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!lastKnown.isLosesAllAbilitiesUntilEndOfTurn()) {
            if (!lastKnown.isFaceDown()) {
                abilities.addAll(withoutLost(gameData, lastKnown.getCard().getId(),
                        lastKnown.getCard().getTriggeredAbilities()));
            }
            abilities.addAll(lastKnown.getPersistentTriggeredAbilities());
            triggered(departure.grantedAbilities(), abilities);
        }
        abilities.addAll(lastKnown.getTemporaryTriggeredAbilities());
        addZoneChangeCandidates(lastKnown.getCard(), lastKnown, departure.controllerId(), Zone.BATTLEFIELD,
                abilities, into);
    }

    /**
     * The cards in graveyards, under their owners' control, except those that went there in this event: they
     * weren't in a graveyard before it.
     */
    private void graveyardCandidates(GameData gameData, List<ZoneChangeEvent> batch, List<TriggerCandidate> into) {
        if (gameQueryService.graveyardCardsHaveLostAllAbilities(gameData)) {
            return;
        }
        Set<UUID> movedTogether = new HashSet<>();
        for (ZoneChangeEvent moved : batch) {
            movedTogether.add(moved.card().getId());
            if (moved.permanent() != null) {
                movedTogether.add(moved.permanent().getCard().getId());
            }
        }
        gameData.playerGraveyards.forEach((ownerId, graveyard) -> {
            for (Card card : List.copyOf(graveyard)) {
                if (movedTogether.contains(card.getId())) {
                    continue;
                }
                List<TriggeredAbility> abilities = new ArrayList<>(withoutLost(gameData, card.getId(),
                        card.getTriggeredAbilities()));
                triggered(gameData.perpetualAbilities(card.getId()), abilities);
                addZoneChangeCandidates(card, null, ownerId, Zone.GRAVEYARD, abilities, into);
            }
        });
    }

    /**
     * The cards in exile that haunted the departed permanent as it left (CR 702.55b), under their owners' control:
     * their "when the creature it haunts dies" abilities trigger from exile (CR 702.55c).
     */
    private void hauntingCandidates(GameData gameData, ZoneChangeEvent event, List<TriggerCandidate> into) {
        for (UUID cardId : event.hauntedBy()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null) {
                continue;
            }
            Card card = exiled.card();
            addZoneChangeCandidates(card, null, exiled.ownerId(), Zone.EXILE,
                    withoutLost(gameData, card.getId(), card.getTriggeredAbilities()), into);
        }
    }

    /**
     * "As long as you control a [type], this has 'Whenever a creature you control dies, ...'" (Relic Vial) is judged
     * just before the creature died (CR 603.10a): a departed permanent that was the controlled [type] still counts.
     * The granted abilities only that look-back turns on.
     */
    private List<TriggeredAbility> lookBackConditionalGrants(GameData gameData, List<ZoneChangeEvent> batch,
                                                             Permanent source, UUID controllerId,
                                                             List<TriggeredAbility> granted) {
        List<TriggeredAbility> lookBack = new ArrayList<>();
        for (CardEffect staticEffect : source.getCard().getStaticEffects()) {
            if (!(staticEffect instanceof ConditionalEffect conditional)
                    || !(conditional.condition() instanceof ControlsPermanent controls)
                    || !(conditional.wrapped() instanceof GrantTriggeredAbilityEffect grant)
                    || grant.scope() != GrantScope.SELF
                    || !(grant.grantedAbility() instanceof TriggeredAbility ability)
                    || granted.contains(ability)
                    || ability.conditions().stream().noneMatch(c -> c.trigger() instanceof ZoneChangeTrigger)) {
                continue;
            }
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(source.getCard().getId())
                    .withSourceControllerId(controllerId);
            List<Permanent> departed = new ArrayList<>();
            for (ZoneChangeEvent departure : batch) {
                if (departure.leftBattlefield() && departure.permanent() != null
                        && controllerId.equals(departure.controllerId())) {
                    departed.add(departure.permanent());
                }
            }
            if (departed.stream().anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                    permanent, controls.filter(), filterContext))) {
                lookBack.add(ability);
            }
        }
        return lookBack;
    }

    private static void addZoneChangeCandidates(Card sourceCard, Permanent sourcePermanent, UUID controllerId,
                                                Zone zone, List<TriggeredAbility> abilities,
                                                List<TriggerCandidate> into) {
        for (TriggeredAbility ability : abilities) {
            if (ability.conditions().stream().anyMatch(condition -> condition.trigger() instanceof ZoneChangeTrigger
                    && condition.zones().functionsIn(zone))) {
                into.add(new TriggerCandidate(sourceCard, sourcePermanent, controllerId, zone, ability));
            }
        }
    }

    /** The triggered abilities of the object that moved in {@code event}. */
    public List<TriggerCandidate> movingObjectCandidates(GameData gameData, ZoneChangeEvent event) {
        List<TriggerCandidate> candidates = new ArrayList<>();
        if (event.leftBattlefield()) {
            departedPermanentCandidates(gameData, event, candidates);
        }
        arrivedCardCandidates(gameData, event, candidates);
        return candidates;
    }

    private void arrivedCardCandidates(GameData gameData, ZoneChangeEvent event, List<TriggerCandidate> into) {
        Card card = event.card();
        Zone zone = event.to();
        if (zone == Zone.GRAVEYARD && gameQueryService.graveyardCardsHaveLostAllAbilities(gameData)) {
            return;
        }
        List<TriggeredAbility> abilities = new ArrayList<>(withoutLost(gameData, card.getId(),
                card.getTriggeredAbilities()));
        triggered(gameData.perpetualAbilities(card.getId()), abilities);
        UUID controllerId = event.ownerId() != null ? event.ownerId() : gameData.ownerOf(card);
        for (TriggeredAbility ability : abilities) {
            into.add(new TriggerCandidate(card, null, controllerId, zone, ability));
        }
    }

    private void departedPermanentCandidates(GameData gameData, ZoneChangeEvent event, List<TriggerCandidate> into) {
        Permanent lastKnown = event.permanent();
        if (lastKnown == null) {
            return;
        }
        Card card = lastKnown.getCard();
        // The snapshot's card is a copy with the characteristics it left with. The abilities printed on it name the
        // card the permanent was (the copied card for a copy), found by identity when it is the moved card itself;
        // the abilities the permanent gained name the card that moved.
        Card printedSource = event.card().getId().equals(card.getId()) ? event.card() : card;
        List<TriggeredAbility> printed = new ArrayList<>();
        List<TriggeredAbility> gained = new ArrayList<>();
        if (!lastKnown.isLosesAllAbilitiesUntilEndOfTurn()) {
            if (!lastKnown.isFaceDown()) {
                printed.addAll(withoutLost(gameData, card.getId(), card.getTriggeredAbilities()));
            }
            gained.addAll(lastKnown.getPersistentTriggeredAbilities());
            triggered(event.grantedAbilities(), gained);
        }
        // An ability granted until end of turn has a later timestamp than most "loses all abilities" effects
        // (Infuse with Vitality after Humble), so, as the slot collectors do, it stays.
        gained.addAll(lastKnown.getTemporaryTriggeredAbilities());
        for (CardEffect delayed : lastKnown.getDelayedDeathEffects()) {
            gained.add(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), delayed));
        }
        for (TriggeredAbility ability : printed) {
            into.add(new TriggerCandidate(printedSource, lastKnown, event.controllerId(), Zone.BATTLEFIELD, ability));
        }
        for (TriggeredAbility ability : gained) {
            into.add(new TriggerCandidate(event.card(), lastKnown, event.controllerId(), Zone.BATTLEFIELD, ability));
        }
    }

    private static void triggered(List<? extends Ability> abilities, List<TriggeredAbility> into) {
        for (Ability ability : abilities) {
            if (ability instanceof TriggeredAbility triggered) {
                into.add(triggered);
            }
        }
    }

    /** {@code abilities} without the effects the card perpetually lost; an ability left with no effect is gone. */
    private static List<TriggeredAbility> withoutLost(GameData gameData, UUID cardId,
                                                      List<TriggeredAbility> abilities) {
        Set<CardEffect> removed = gameData.perpetuallyRemovedTriggeredAbilities.get(cardId);
        if (removed == null || removed.isEmpty()) {
            return abilities;
        }
        List<TriggeredAbility> kept = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<EffectRegistration> effects = ability.effects().stream()
                    .filter(registration -> !removed.contains(registration.effect()))
                    .toList();
            if (effects.size() == ability.effects().size()) {
                kept.add(ability);
            } else if (!effects.isEmpty() && !effects.getFirst().simultaneousWithPrevious()) {
                kept.add(ability.withEffects(effects));
            }
        }
        return kept;
    }
}
