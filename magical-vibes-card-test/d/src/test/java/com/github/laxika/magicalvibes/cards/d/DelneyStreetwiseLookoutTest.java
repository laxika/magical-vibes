package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelneyStreetwiseLookout.class, GrizzlyBears.class, HillGiant.class, MentorOfTheMeek.class,
        NoviceInspector.class, Shock.class, DoomedTraveler.class, DeadlyCoverUp.class,
        DorotheaVengefulVictim.class, DorotheasRetribution.class})
class DelneyStreetwiseLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with power 2 or less cannot be blocked by power 3 or greater")
    void smallCreaturesCannotBeBlockedByLargeCreatures() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent largeBlocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with power 3 or greater");
        assertThat(largeBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with power 2 can block a small creature")
    void powerTwoCreatureCanBlockSmallCreature() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Delney makes a qualifying creature's triggered ability trigger twice")
    void doublesQualifyingCreatureTrigger() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        addCreatureReady(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void delneyItselfCannotBeBlockedByPowerThreeCreature() {
        Permanent delney = addCreatureReady(player1, new DelneyStreetwiseLookout());
        delney.setAttacking(true);
        addCreatureReady(player2, new HillGiant());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with power 3 or greater");
    }

    @Test
    void powerThreeAttackerCanBeBlockedByPowerThreeCreature() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blockerPowerIncludesCounters() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with power 3 or greater");
    }

    @Test
    void opponentsSmallCreaturesDoNotReceiveBlockingProtection() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent blocker = addCreatureReady(player1, new HillGiant());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void doublesEnteringCreaturesOwnAbilityAndKeepsBothTriggersAfterPowerIncrease() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        findPermanent(player1, "Novice Inspector").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
    }

    @Test
    void doesNotDoubleAbilityOfCreatureWithPowerThree() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent mentor = addCreatureReady(player1, new MentorOfTheMeek());
        mentor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof MentorOfTheMeek).hasSize(1);
        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof NoviceInspector).hasSize(2);
    }

    @Test
    void doesNotDoubleOpponentsCreatureAbility() {
        addCreatureReady(player2, new DelneyStreetwiseLookout());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    @Test
    void doublesTriggeredAbilityGrantedToDelneyItself() {
        Permanent delney = addCreatureReady(player1, new DelneyStreetwiseLookout());
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveFlashback(player1, 0, delney.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> assertThat(entry.getSourcePermanentId()).isEqualTo(delney.getId()));
    }

    @Test
    void doublesSmallCreaturesDeathTrigger() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent traveler = addCreatureReady(player1, new DoomedTraveler());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, traveler.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Doomed Traveler");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    void doublesDeathTriggerWhenDelneyDiesSimultaneously() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        addCreatureReady(player1, new DoomedTraveler());
        harness.setHand(player1, List.of(new DeadlyCoverUp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Delney, Streetwise Lookout");
        harness.assertInGraveyard(player1, "Doomed Traveler");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }
}
