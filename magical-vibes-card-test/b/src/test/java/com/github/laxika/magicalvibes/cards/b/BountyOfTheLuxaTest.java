package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.ForsakeTheWorldly;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BountyOfTheLuxa.class, Colossapede.class, ForsakeTheWorldly.class})
class BountyOfTheLuxaTest extends BaseCardTest {

    @Test
    @DisplayName("With no flood counters: puts a flood counter and draws a card")
    void noCountersPutsCounterAndDraws() {
        Permanent bounty = addBounty(player1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede(), new Colossapede()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("With a flood counter: removes it and adds {C}{G}{U} instead of drawing")
    void withCounterRemovesAndAddsMana() {
        Permanent bounty = addBounty(player1);
        bounty.setCounterCount(CounterType.FLOOD, 1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede(), new Colossapede()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Alternates between drawing and ramping over consecutive turns")
    void alternatesEachTurn() {
        Permanent bounty = addBounty(player1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede(), new Colossapede()));

        // First main phase: no counters -> gains a flood counter.
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(1);

        // Next turn's first main phase: the counter is removed and mana is added.
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(0);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        addBounty(player1);

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removesAllFloodCountersButAddsManaOnlyOnce() {
        Permanent bounty = addBounty(player1);
        bounty.setCounterCount(CounterType.FLOOD, 3);
        bounty.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isZero();
        assertThat(bounty.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void drawsWhenSourceWithFloodCounterIsExiledInResponse() {
        Permanent bounty = addBounty(player1);
        bounty.setCounterCount(CounterType.FLOOD, 1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede()));
        harness.setHand(player2, List.of(new ForsakeTheWorldly()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, bounty.getId());
        harness.assertNotOnBattlefield(player1, "Bounty of the Luxa");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void checksFloodCountersOnResolutionRatherThanWhenAbilityTriggers() {
        Permanent bounty = addBounty(player1);
        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.stack).hasSize(1);
        bounty.setCounterCount(CounterType.FLOOD, 1);

        harness.passBothPriorities();

        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void drawsIfFloodCounterIsRemovedBeforeResolution() {
        Permanent bounty = addBounty(player1);
        bounty.setCounterCount(CounterType.FLOOD, 1);
        harness.setLibrary(player1, List.of(new Colossapede(), new Colossapede()));
        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.stack).hasSize(1);
        bounty.setCounterCount(CounterType.FLOOD, 0);

        harness.passBothPriorities();

        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void doesNotTriggerOnPostcombatMainPhase() {
        Permanent bounty = addBounty(player1);
        bounty.setCounterCount(CounterType.FLOOD, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(bounty.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    private Permanent addBounty(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BountyOfTheLuxa());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
