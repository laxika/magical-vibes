package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.m.MassPolymorph;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BramblewoodParagon.class, ElvishWarrior.class, Lignify.class, MassPolymorph.class, PricklyBoggart.class})
class BramblewoodParagonTest extends BaseCardTest {

    // ===== Static: other Warriors you control enter with an additional +1/+1 counter =====

    @Test
    @DisplayName("Another Warrior you control enters with an additional +1/+1 counter")
    void otherWarriorEntersWithCounter() {
        addCreatureReady(player1, new BramblewoodParagon());

        harness.castFromHand(player1, new BramblewoodParagon(), "{1}{G}");
        harness.passBothPriorities();

        Permanent entered = findPermanents(player1, "Bramblewood Paragon").get(1);
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A lone Paragon does not give itself a counter (\"other\")")
    void loneParagonGetsNoCounter() {
        harness.castFromHand(player1, new BramblewoodParagon(), "{1}{G}");
        harness.passBothPriorities();

        Permanent paragon = findPermanents(player1, "Bramblewood Paragon").get(0);
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Warrior creature does not get a counter")
    void nonWarriorGetsNoCounter() {
        addCreatureReady(player1, new BramblewoodParagon());

        harness.castFromHand(player1, new PricklyBoggart(), "{B}");
        harness.passBothPriorities();

        Permanent boggart = findPermanent(player1, "Prickly Boggart");
        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Warrior entering under an opponent's control does not get a counter")
    void opponentWarriorGetsNoCounter() {
        addCreatureReady(player1, new BramblewoodParagon());

        Permanent warrior = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== Static: creatures you control with a +1/+1 counter have trample =====

    @Test
    @DisplayName("Own creature with a +1/+1 counter gains trample")
    void counteredCreatureGainsTrample() {
        addCreatureReady(player1, new BramblewoodParagon());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        warrior.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Own creature without a +1/+1 counter does not have trample")
    void uncounteredCreatureHasNoTrample() {
        addCreatureReady(player1, new BramblewoodParagon());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The Paragon itself gains trample once it has a +1/+1 counter")
    void paragonGainsTrampleWithCounter() {
        Permanent paragon = addCreatureReady(player1, new BramblewoodParagon());
        paragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The Paragon has no trample while it has no +1/+1 counter")
    void paragonHasNoTrampleWithoutCounter() {
        Permanent paragon = addCreatureReady(player1, new BramblewoodParagon());

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's countered creature does not gain trample")
    void opponentCounteredCreatureHasNoTrample() {
        addCreatureReady(player1, new BramblewoodParagon());
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        warrior.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isFalse();
    }

    /**
     * CR 614.12 / official ruling: "If Bramblewood Paragon enters at the same time as another
     * Warrior (due to Living End, for example), that creature doesn't get a +1/+1 counter."
     * Mass Polymorph puts both Paragons onto the battlefield simultaneously, so neither may apply
     * its replacement effect to the other even though the engine places them one at a time.
     */
    @Test
    @DisplayName("Paragons entering simultaneously via Mass Polymorph give each other no counter")
    void simultaneousParagonsGiveNoCounters() {
        harness.addToBattlefield(player1, new PricklyBoggart());
        harness.addToBattlefield(player1, new PricklyBoggart());
        harness.setLibrary(player1, List.of(new BramblewoodParagon(), new BramblewoodParagon()));

        harness.castFromHand(player1, new MassPolymorph(), "{5}{U}");
        harness.passBothPriorities();

        List<Permanent> paragons = findPermanents(player1, "Bramblewood Paragon");
        assertThat(paragons).hasSize(2);
        assertThat(paragons).allSatisfy(paragon ->
                assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }


    @Test
    @DisplayName("Multiple Paragons each add a counter to an entering Warrior")
    void multipleParagonsAddCounters() {
        harness.addToBattlefield(player1, new BramblewoodParagon());
        harness.addToBattlefield(player1, new BramblewoodParagon());

        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Warrior with a +1/+1 counter also has trample")
    void counteredNonWarriorGainsTrample() {
        harness.addToBattlefield(player1, new BramblewoodParagon());
        Permanent boggart = harness.addToBattlefieldAndReturn(player1, new PricklyBoggart());
        boggart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, boggart, Keyword.TRAMPLE)).isTrue();

        boggart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, boggart, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removing Paragon removes granted trample but leaves entry counters")
    void removingParagonLeavesCountersButRemovesTrample() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new BramblewoodParagon());
        Permanent warrior = harness.enterBattlefieldAndReturn(player1, new ElvishWarrior());

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(paragon);

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Lignified Paragon cannot add entry counters or grant trample")
    void lignifiedParagonDoesNotApplyItsAbilities() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new BramblewoodParagon());
        Permanent boggart = harness.addToBattlefieldAndReturn(player1, new PricklyBoggart());
        boggart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, boggart, Keyword.TRAMPLE)).isTrue();

        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, paragon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, boggart, Keyword.TRAMPLE)).isFalse();

        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isFalse();
    }
}
