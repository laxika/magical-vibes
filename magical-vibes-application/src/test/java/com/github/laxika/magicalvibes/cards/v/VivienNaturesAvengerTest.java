package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienNaturesAvenger.class, GreenwoodSentinel.class, Forest.class, Mountain.class, Shock.class})
class VivienNaturesAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts three +1/+1 counters on up to one target creature")
    void plusOnePutsCountersOnTargetCreature() {
        Permanent vivien = addReadyVivien(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 can resolve without a target")
    void plusOneCanChooseNoTarget() {
        Permanent vivien = addReadyVivien(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 puts the first revealed creature card into hand and the rest on the library bottom")
    void minusOneFindsCreatureCard() {
        addReadyVivien(4);
        Card forest = new Forest();
        Card shock = new Shock();
        Card creature = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(forest, shock, creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
    }

    @Test
    @DisplayName("-6 gives a target creature +10/+10 and trample until end of turn")
    void minusSixBoostsTargetAndGrantsTrample() {
        Permanent vivien = addReadyVivien(6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("-6 cannot target a noncreature permanent")
    void minusSixRejectsNonCreatureTarget() {
        Permanent vivien = addReadyVivien(6);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 can put counters on an opponent's creature")
    void plusOneCanTargetOpponentCreature() {
        Permanent vivien = addReadyVivien(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 leaves unrevealed cards above the randomly ordered revealed cards")
    void minusOneStopsAtFirstCreature() {
        Permanent vivien = addReadyVivien(4);
        Card forest = new Forest();
        Card creature = new GreenwoodSentinel();
        Card shock = new Shock();
        Card mountain = new Mountain();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, creature, shock, mountain));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, mountain, forest);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-1 returns all revealed cards to the library when there is no creature")
    void minusOneWithNoCreature() {
        addReadyVivien(4);
        Card forest = new Forest();
        Card shock = new Shock();
        Card mountain = new Mountain();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, shock, mountain));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock, mountain);
    }

    @Test
    @DisplayName("-1 does nothing to an empty library")
    void minusOneWithEmptyLibrary() {
        Permanent vivien = addReadyVivien(4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 can boost an opponent's creature")
    void minusSixCanTargetOpponentCreature() {
        addReadyVivien(7);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyVivien(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new VivienNaturesAvenger());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
