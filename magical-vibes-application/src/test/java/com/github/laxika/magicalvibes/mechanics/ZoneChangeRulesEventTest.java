package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.Griptide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RaiseDead;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.ShadowTriggerRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every choke point where a card changes zones emits one {@link ZoneChangeEvent}, with the right zones, cause and
 * snapshot, grouped into one batch per occurrence (CR 603.2c, CR 603.10a).
 */
class ZoneChangeRulesEventTest extends BaseCardTest {

    private ShadowTriggerRecorder recorder;

    @BeforeEach
    void startRecording() {
        recorder = harness.getShadowRecorder();
        recorder.clear();
    }

    @Test
    void aDestroyedCreatureLeavesWithItsLastKnownPowerAndController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new Terror()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        recorder.clear();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent death = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(death.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(death.to()).isEqualTo(Zone.GRAVEYARD);
        assertThat(death.cause()).isEqualTo(ZoneChangeCause.DESTROY);
        assertThat(death.controllerId()).isEqualTo(player2.getId());
        assertThat(death.permanent().getId()).isEqualTo(bears.getId());
        assertThat(death.permanent().getLastKnownPower()).isEqualTo(5);
    }

    @Test
    void aWrathIsOneBatchHoldingEveryDeath() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        castAndResolveSorcery();

        List<RulesEventBatch> deathBatches = recorder.batches().stream()
                .filter(batch -> batch.events().stream()
                        .anyMatch(event -> event instanceof ZoneChangeEvent zoneChange && zoneChange.leftBattlefield()))
                .toList();
        assertThat(deathBatches).hasSize(1);
        assertThat(deathBatches.getFirst().events()).hasSize(3)
                .map(event -> ((ZoneChangeEvent) event).card())
                .containsExactlyInAnyOrder(bears.getOriginalCard(), elves.getOriginalCard(),
                        otherBears.getOriginalCard());
        assertThat(deathBatches.getFirst().events())
                .allSatisfy(event -> assertThat(((ZoneChangeEvent) event).cause()).isEqualTo(ZoneChangeCause.DESTROY));
    }

    @Test
    void aSacrificedCreatureIsCausedBySacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        ZoneChangeEvent death = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(death.to()).isEqualTo(Zone.GRAVEYARD);
        assertThat(death.cause()).isEqualTo(ZoneChangeCause.SACRIFICE);
    }

    @Test
    void lethalDamageIsAStateBasedDestruction() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent death = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(death.leftBattlefield()).isTrue();
        assertThat(death.to()).isEqualTo(Zone.GRAVEYARD);
        assertThat(death.cause()).isEqualTo(ZoneChangeCause.DESTROY);
    }

    @Test
    void aBouncedPermanentGoesToHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent bounce = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(bounce.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(bounce.to()).isEqualTo(Zone.HAND);
        assertThat(bounce.cause()).isEqualTo(ZoneChangeCause.OTHER);
        assertThat(bounce.permanent().getId()).isEqualTo(bears.getId());
    }

    @Test
    void anExiledPermanentGoesToExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent exile = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(exile.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(exile.to()).isEqualTo(Zone.EXILE);
    }

    @Test
    void aPermanentPutOnTopOfItsLibraryGoesToTheLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Griptide()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent move = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(move.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(move.to()).isEqualTo(Zone.LIBRARY);
    }

    @Test
    void aDeathAReplacementExilesInsteadIsOneZoneChangeToExile() {
        harness.addToBattlefield(player1, new RestInPeace());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        ZoneChangeEvent exile = onlyZoneChangeOf(bears.getOriginalCard());
        assertThat(exile.from()).isEqualTo(Zone.BATTLEFIELD);
        assertThat(exile.to()).isEqualTo(Zone.EXILE);
        assertThat(exile.cause()).isEqualTo(ZoneChangeCause.DESTROY);
    }

    @Test
    void createdTokensComeFromNowhereAndDieToTheGraveyard() {
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        List<ZoneChangeEvent> entries = recorder.zoneChanges().stream()
                .filter(event -> event.card().isToken()).toList();
        assertThat(entries).hasSize(2).allSatisfy(entry -> {
            assertThat(entry.from()).isNull();
            assertThat(entry.to()).isEqualTo(Zone.BATTLEFIELD);
            assertThat(entry.controllerId()).isEqualTo(player1.getId());
        });
        recorder.clear();

        castAndResolveSorcery();

        assertThat(recorder.zoneChanges().stream().filter(event -> event.card().isToken()).toList())
                .hasSize(2).allSatisfy(death -> {
                    assertThat(death.from()).isEqualTo(Zone.BATTLEFIELD);
                    assertThat(death.to()).isEqualTo(Zone.GRAVEYARD);
                });
    }

    @Test
    void aCastCreatureMovesFromHandToStackToBattlefield() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<ZoneChangeEvent> moves = zoneChangesOf(bears);
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0).from()).isEqualTo(Zone.HAND);
        assertThat(moves.get(0).to()).isEqualTo(Zone.STACK);
        assertThat(moves.get(0).cause()).isEqualTo(ZoneChangeCause.CAST);
        assertThat(moves.get(1).from()).isEqualTo(Zone.STACK);
        assertThat(moves.get(1).enteredBattlefield()).isTrue();
        assertThat(moves.get(1).controllerId()).isEqualTo(player1.getId());
        assertThat(moves.get(1).permanent().getId()).isEqualTo(findPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    void aResolvedInstantMovesFromStackToGraveyard() {
        Card unsummon = new Unsummon();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        List<ZoneChangeEvent> moves = zoneChangesOf(unsummon);
        assertThat(moves).extracting(ZoneChangeEvent::from).containsExactly(Zone.HAND, Zone.STACK);
        assertThat(moves).extracting(ZoneChangeEvent::to).containsExactly(Zone.STACK, Zone.GRAVEYARD);
    }

    @Test
    void milledCardsAreOneBatchCausedByTheMill() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        List<Card> topFive = List.copyOf(gd.playerDecks.get(player2.getId()).subList(0, 5));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        List<RulesEventBatch> millBatches = recorder.batches().stream()
                .filter(batch -> batch.events().stream()
                        .anyMatch(event -> event instanceof ZoneChangeEvent zoneChange
                                && zoneChange.cause() == ZoneChangeCause.MILL))
                .toList();
        assertThat(millBatches).hasSize(1);
        assertThat(millBatches.getFirst().events()).hasSize(5).allSatisfy(event -> {
            ZoneChangeEvent mill = (ZoneChangeEvent) event;
            assertThat(mill.from()).isEqualTo(Zone.LIBRARY);
            assertThat(mill.to()).isEqualTo(Zone.GRAVEYARD);
            assertThat(mill.cause()).isEqualTo(ZoneChangeCause.MILL);
        });
        assertThat(millBatches.getFirst().events()).map(event -> ((ZoneChangeEvent) event).card())
                .containsExactlyElementsOf(topFive);
    }

    @Test
    void discardedCardsAreCausedByTheDiscard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        for (Card discarded : List.of(first, second)) {
            ZoneChangeEvent discard = onlyZoneChangeOf(discarded);
            assertThat(discard.from()).isEqualTo(Zone.HAND);
            assertThat(discard.to()).isEqualTo(Zone.GRAVEYARD);
            assertThat(discard.cause()).isEqualTo(ZoneChangeCause.DISCARD);
        }
    }

    @Test
    void drawnCardsMoveFromLibraryToHand() {
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        List<Card> topTwo = List.copyOf(gd.playerDecks.get(player1.getId()).subList(0, 2));

        castAndResolveSorcery();

        for (Card drawn : topTwo) {
            ZoneChangeEvent draw = onlyZoneChangeOf(drawn);
            assertThat(draw.from()).isEqualTo(Zone.LIBRARY);
            assertThat(draw.to()).isEqualTo(Zone.HAND);
        }
    }

    @Test
    void aCardReturnedFromTheGraveyardMovesToHand() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RaiseDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        ZoneChangeEvent move = onlyZoneChangeOf(target);
        assertThat(move.from()).isEqualTo(Zone.GRAVEYARD);
        assertThat(move.to()).isEqualTo(Zone.HAND);
    }

    @Test
    void aCardExiledFromTheGraveyardMovesToExile() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new CoffinPurge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        ZoneChangeEvent move = onlyZoneChangeOf(target);
        assertThat(move.from()).isEqualTo(Zone.GRAVEYARD);
        assertThat(move.to()).isEqualTo(Zone.EXILE);
    }

    @Test
    void reorderingALibraryIsNoZoneChange() {
        List<Card> library = gd.playerDecks.get(player1.getId());
        Card top = library.removeFirst();

        library.add(top);

        assertThat(recorder.zoneChanges()).isEmpty();
    }

    @Test
    void eventsAreDrainedBeforeAPlayerReceivesPriority() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.rulesEvents.pending()).isEmpty();
        assertThat(recorder.zoneChanges()).isNotEmpty();
    }

    @Test
    void theSlotCollectorsTriggersAreRecorded() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new SoulWarden());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(recorder.oldPath()).anySatisfy(queued ->
                assertThat(queued.trigger().effectClasses()).isNotEmpty());
        assertThat(recorder.differences().isEmpty()).isTrue();
    }

    private void castAndResolveSorcery() {
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private List<ZoneChangeEvent> zoneChangesOf(Card card) {
        return recorder.zoneChanges().stream().filter(event -> event.card().getId().equals(card.getId())).toList();
    }

    private ZoneChangeEvent onlyZoneChangeOf(Card card) {
        List<ZoneChangeEvent> moves = zoneChangesOf(card);
        assertThat(moves).hasSize(1);
        return moves.getFirst();
    }
}
