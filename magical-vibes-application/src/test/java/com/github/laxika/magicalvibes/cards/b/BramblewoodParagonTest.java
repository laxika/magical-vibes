package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassPolymorph;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BramblewoodParagonTest extends BaseCardTest {

    // ===== Static: other Warriors you control enter with an additional +1/+1 counter =====

    @Test
    @DisplayName("Another Warrior you control enters with an additional +1/+1 counter")
    void otherWarriorEntersWithCounter() {
        addReadyParagon(player1);

        harness.setHand(player1, List.of(new BramblewoodParagon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = paragonsOnBattlefield(player1).get(1);
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A lone Paragon does not give itself a counter (\"other\")")
    void loneParagonGetsNoCounter() {
        harness.setHand(player1, List.of(new BramblewoodParagon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paragon = paragonsOnBattlefield(player1).get(0);
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Warrior creature does not get a counter")
    void nonWarriorGetsNoCounter() {
        addReadyParagon(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = creatureNamed(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== Static: creatures you control with a +1/+1 counter have trample =====

    @Test
    @DisplayName("Own creature with a +1/+1 counter gains trample")
    void counteredCreatureGainsTrample() {
        addReadyParagon(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Own creature without a +1/+1 counter does not have trample")
    void uncounteredCreatureHasNoTrample() {
        addReadyParagon(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The Paragon itself gains trample once it has a +1/+1 counter")
    void paragonGainsTrampleWithCounter() {
        Permanent paragon = addReadyParagon(player1);
        paragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The Paragon has no trample while it has no +1/+1 counter")
    void paragonHasNoTrampleWithoutCounter() {
        Permanent paragon = addReadyParagon(player1);

        assertThat(gqs.hasKeyword(gd, paragon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's countered creature does not gain trample")
    void opponentCounteredCreatureHasNoTrample() {
        addReadyParagon(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new BramblewoodParagon());
        gd.playerDecks.get(player1.getId()).add(new BramblewoodParagon());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> paragons = paragonsOnBattlefield(player1);
        assertThat(paragons).hasSize(2);
        assertThat(paragons).allSatisfy(paragon ->
                assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    // ===== Helpers =====

    private Permanent addReadyParagon(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BramblewoodParagon());
        perm.setSummoningSick(false);
        return perm;
    }

    private List<Permanent> paragonsOnBattlefield(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanents(player, "Bramblewood Paragon");
    }

    private Permanent creatureNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return findPermanent(player, name);
    }
}
