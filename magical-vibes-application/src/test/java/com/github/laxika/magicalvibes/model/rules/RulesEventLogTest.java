package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RulesEventLogTest {

    private final RulesEventLog log = new RulesEventLog();
    private final UUID owner = UUID.randomUUID();

    @Test
    void anArrivalNamesTheZoneTheCardWasLastSeenIn() {
        Card card = new Card();
        log.track(card, null, Zone.LIBRARY);

        log.arrived(card, owner, Zone.HAND);

        ZoneChangeEvent event = onlyEvent();
        assertThat(event.card()).isSameAs(card);
        assertThat(event.ownerId()).isEqualTo(owner);
        assertThat(event.from()).isEqualTo(Zone.LIBRARY);
        assertThat(event.to()).isEqualTo(Zone.HAND);
        assertThat(event.cause()).isEqualTo(ZoneChangeCause.OTHER);
        assertThat(event.permanent()).isNull();
        assertThat(log.zoneOf(card.getId())).isEqualTo(Zone.HAND);
    }

    @Test
    void aCardNobodySawBeforeComesFromNowhere() {
        Card card = new Card();

        log.arrived(card, owner, Zone.GRAVEYARD);

        assertThat(onlyEvent().from()).isNull();
    }

    @Test
    void aMoveInsideOneZoneIsNoZoneChange() {
        Card card = new Card();
        log.track(card, null, Zone.LIBRARY);

        log.arrived(card, owner, Zone.LIBRARY);

        assertThat(log.pending()).isEmpty();
    }

    @Test
    void eventsOutsideAScopeAreBatchesOfTheirOwn() {
        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.arrived(new Card(), owner, Zone.GRAVEYARD);

        assertThat(log.pending()).hasSize(2).allSatisfy(batch -> assertThat(batch.events()).hasSize(1));
    }

    @Test
    void nestedScopesShareOneBatchThatTheOutermostCloses() {
        log.beginBatch();
        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.beginBatch();
        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.endBatch();
        assertThat(log.drain()).isEmpty();
        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.endBatch();

        List<RulesEventBatch> drained = log.drain();
        assertThat(drained).hasSize(1);
        assertThat(drained.getFirst().events()).hasSize(3);
        assertThat(log.isBatchOpen()).isFalse();
    }

    @Test
    void anIsolatedBatchClosesInsideAnOpenScopeAndHandsTheScopeBack() {
        log.beginBatch();
        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.beginIsolatedBatch();
        log.arrived(new Card(), owner, Zone.EXILE);
        log.arrived(new Card(), owner, Zone.EXILE);
        log.endIsolatedBatch();

        List<RulesEventBatch> isolated = log.drain();
        assertThat(isolated).hasSize(1);
        assertThat(isolated.getFirst().events()).hasSize(2)
                .allSatisfy(event -> assertThat(((ZoneChangeEvent) event).to()).isEqualTo(Zone.EXILE));
        assertThat(log.isBatchOpen()).isTrue();

        log.arrived(new Card(), owner, Zone.GRAVEYARD);
        log.endBatch();

        List<RulesEventBatch> outer = log.drain();
        assertThat(outer).hasSize(1);
        assertThat(outer.getFirst().events()).hasSize(2)
                .allSatisfy(event -> assertThat(((ZoneChangeEvent) event).to()).isEqualTo(Zone.GRAVEYARD));
        assertThat(log.isBatchOpen()).isFalse();
    }

    @Test
    void anEmptyScopeLeavesNoBatch() {
        log.beginBatch();
        log.endBatch();

        assertThat(log.pending()).isEmpty();
        assertThatThrownBy(() -> new RulesEventBatch(1, List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void drainTakesTheClosedBatchesOnce() {
        log.arrived(new Card(), owner, Zone.GRAVEYARD);

        assertThat(log.drain()).hasSize(1);
        assertThat(log.drain()).isEmpty();
        assertThat(log.pending()).isEmpty();
    }

    @Test
    void aDepartureCarriesItsSnapshotAndController() {
        Permanent permanent = new Permanent(new Card());
        Permanent lastKnown = new Permanent(permanent);
        UUID controller = UUID.randomUUID();

        log.departed(lastKnown, permanent.getOriginalCard(), List.of(permanent.getOriginalCard().getId()), owner,
                controller, Zone.GRAVEYARD);

        ZoneChangeEvent event = onlyEvent();
        assertThat(event.leftBattlefield()).isTrue();
        assertThat(event.to()).isEqualTo(Zone.GRAVEYARD);
        assertThat(event.permanent()).isSameAs(lastKnown);
        assertThat(event.controllerId()).isEqualTo(controller);
    }

    @Test
    void theDepartingCardsArrivalIsTheSameZoneChange() {
        Card card = new Card();
        Permanent permanent = new Permanent(card);

        log.departed(new Permanent(permanent), card, List.of(card.getId()), owner, null, Zone.GRAVEYARD);
        log.arrived(card, owner, Zone.GRAVEYARD);

        assertThat(onlyEvent().from()).isEqualTo(Zone.BATTLEFIELD);
    }

    @Test
    void aReplacementThatSendsTheCardElsewhereRewritesTheDeparture() {
        Card card = new Card();
        Permanent permanent = new Permanent(card);

        log.departed(new Permanent(permanent), card, List.of(card.getId()), owner, null, Zone.GRAVEYARD);
        log.arrived(card, owner, Zone.EXILE);

        ZoneChangeEvent event = onlyEvent();
        assertThat(event.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(event.to()).isEqualTo(Zone.EXILE);
        assertThat(log.zoneOf(card.getId())).isEqualTo(Zone.EXILE);
    }

    @Test
    void aCardThatArrivedMovesOnAsANewZoneChange() {
        Card card = new Card();
        Permanent permanent = new Permanent(card);

        log.departed(new Permanent(permanent), card, List.of(card.getId()), owner, null, Zone.GRAVEYARD);
        log.arrived(card, owner, Zone.GRAVEYARD);
        log.arrived(card, owner, Zone.EXILE);

        List<RulesEventBatch> pending = log.pending();
        assertThat(pending).hasSize(2);
        ZoneChangeEvent second = (ZoneChangeEvent) pending.get(1).events().getFirst();
        assertThat(second.from()).isEqualTo(Zone.GRAVEYARD);
        assertThat(second.to()).isEqualTo(Zone.EXILE);
    }

    @Test
    void anEntryComesFromTheTrackedZoneAndCarriesACopy() {
        Card card = new Card();
        log.track(card, null, Zone.STACK);
        Permanent permanent = new Permanent(card);
        UUID controller = UUID.randomUUID();

        log.entered(permanent, owner, controller);

        ZoneChangeEvent event = onlyEvent();
        assertThat(event.from()).isEqualTo(Zone.STACK);
        assertThat(event.enteredBattlefield()).isTrue();
        assertThat(event.permanent()).isNotSameAs(permanent);
        assertThat(event.permanent().getId()).isEqualTo(permanent.getId());
        assertThat(event.controllerId()).isEqualTo(controller);
    }

    @Test
    void aRetractedEntryLeavesNoEventAndTheCardWhereItCameFrom() {
        Card card = new Card();
        log.track(card, null, Zone.STACK);
        Permanent permanent = new Permanent(card);
        log.entered(permanent, owner, null);

        log.retractEntry(permanent.getId());
        log.arrived(card, owner, Zone.EXILE);

        ZoneChangeEvent event = onlyEvent();
        assertThat(event.from()).isEqualTo(Zone.STACK);
        assertThat(event.to()).isEqualTo(Zone.EXILE);
    }

    @Test
    void aCauseAppliesToItsObjectOnlyUntilRestored() {
        Card discarded = new Card();
        Card other = new Card();
        ZoneChangeCause previous = log.setCause(discarded.getId(), ZoneChangeCause.DISCARD);

        log.arrived(discarded, owner, Zone.GRAVEYARD);
        log.arrived(other, owner, Zone.GRAVEYARD);
        log.restoreCause(discarded.getId(), previous);
        log.arrived(discarded, owner, Zone.EXILE);

        List<RulesEventBatch> pending = log.pending();
        assertThat(((ZoneChangeEvent) pending.get(0).events().getFirst()).cause()).isEqualTo(ZoneChangeCause.DISCARD);
        assertThat(((ZoneChangeEvent) pending.get(1).events().getFirst()).cause()).isEqualTo(ZoneChangeCause.OTHER);
        assertThat(((ZoneChangeEvent) pending.get(2).events().getFirst()).cause()).isEqualTo(ZoneChangeCause.OTHER);
    }

    @Test
    void aDepartureWhileASpellIsBeingCastCarriesItUntilRestored() {
        Card spell = new Card();
        Card sacrificed = new Card();
        Card later = new Card();
        Card previous = log.setCastingSpell(spell);

        log.departed(new Permanent(sacrificed), sacrificed, List.of(sacrificed.getId()), owner, null, Zone.GRAVEYARD);
        log.restoreCastingSpell(previous);
        log.departed(new Permanent(later), later, List.of(later.getId()), owner, null, Zone.GRAVEYARD);

        List<RulesEventBatch> pending = log.pending();
        assertThat(((ZoneChangeEvent) pending.get(0).events().getFirst()).castingSpell()).isSameAs(spell);
        assertThat(((ZoneChangeEvent) pending.get(1).events().getFirst()).castingSpell()).isNull();
    }

    @Test
    void aCopyKeepsTheSpellBeingCast() {
        Card spell = new Card();
        Card sacrificed = new Card();
        log.setCastingSpell(spell);

        RulesEventLog copy = new RulesEventLog();
        copy.copyFrom(log);
        copy.departed(new Permanent(sacrificed), sacrificed, List.of(sacrificed.getId()), owner, null, Zone.GRAVEYARD);

        assertThat(((ZoneChangeEvent) copy.drain().getFirst().events().getFirst()).castingSpell()).isSameAs(spell);
    }

    @Test
    void whileSuppressedNothingIsEmittedOrTracked() {
        Card card = new Card();
        log.suppress();

        log.arrived(card, owner, Zone.HAND);
        log.track(card, null, Zone.LIBRARY);
        log.resume();

        assertThat(log.pending()).isEmpty();
        assertThat(log.zoneOf(card.getId())).isNull();
    }

    @Test
    void aCopySharesNoSnapshotAndKeepsWaitingDepartures() {
        Card card = new Card();
        Permanent permanent = new Permanent(card);
        log.beginBatch();
        log.departed(new Permanent(permanent), card, List.of(card.getId()), owner, null, Zone.GRAVEYARD);

        RulesEventLog copy = new RulesEventLog();
        copy.copyFrom(log);
        copy.arrived(card, owner, Zone.EXILE);
        copy.endBatch();

        ZoneChangeEvent original = (ZoneChangeEvent) log.pending().getFirst().events().getFirst();
        ZoneChangeEvent copied = (ZoneChangeEvent) copy.drain().getFirst().events().getFirst();
        assertThat(copied.permanent()).isNotSameAs(original.permanent());
        assertThat(copied.to()).isEqualTo(Zone.EXILE);
        assertThat(original.to()).isEqualTo(Zone.GRAVEYARD);
        assertThat(log.isBatchOpen()).isTrue();
    }

    private ZoneChangeEvent onlyEvent() {
        List<RulesEventBatch> pending = log.pending();
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().events()).hasSize(1);
        return (ZoneChangeEvent) pending.getFirst().events().getFirst();
    }
}
