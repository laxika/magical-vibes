package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackstreetBruiser.class, GrizzlyBears.class, Island.class})
class BackstreetBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack with fewer than two counters among controlled creatures")
    void cannotAttackWithFewerThanTwoCounters() {
        addReadyBruiser();
        Permanent helper = addReadyCreature(player1);
        helper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addReadyCreature(player2);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Two counters on one creature allow it to attack despite defender")
    void twoCountersOnOneCreatureAllowAttack() {
        Permanent bruiser = addReadyBruiser();
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addReadyCreature(player2);

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(bruiser.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Counters on separate controlled creatures count together")
    void countersOnSeparateCreaturesCountTogether() {
        Permanent bruiser = addReadyBruiser();
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent helper = addReadyCreature(player1);
        helper.setCounterCount(CounterType.CHARGE, 1);
        addReadyCreature(player2);

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(bruiser.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Counters on an opponent's creature do not count")
    void opponentCountersDoNotCount() {
        addReadyBruiser();
        Permanent opponentCreature = addReadyCreature(player2);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Counters on a noncreature permanent do not count")
    void noncreatureCountersDoNotCount() {
        addReadyBruiser();
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.CHARGE, 2);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Removing a counter before declaration restores the defender restriction")
    void removingCounterBeforeDeclarationPreventsAttack() {
        Permanent bruiser = addReadyBruiser();
        Permanent helper = addReadyCreature(player1);
        helper.setCounterCount(CounterType.CHARGE, 2);
        assertThat(harness.getAttackLegalityService().canAttack(gd, bruiser, player1.getId())).isTrue();
        helper.setCounterCount(CounterType.CHARGE, 1);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(bruiser.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Meeting the counter threshold does not bypass summoning sickness")
    void countersDoNotBypassSummoningSickness() {
        Permanent bruiser = addReadyBruiser();
        bruiser.setSummoningSick(true);
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(bruiser.isAttacking()).isFalse();
    }

    private Permanent addReadyBruiser() {
        return addReadyCreature(player1, new BackstreetBruiser());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyCreature(player, new GrizzlyBears());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
