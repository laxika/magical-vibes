package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WiltLeafCavaliers;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeechBonder.class, WiltLeafCavaliers.class, Tatterkite.class})
class LeechBonderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (3/3 becomes 1/1)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeechBonder()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent bonder = findBonder(player1);
        assertThat(bonder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        assertThat(gd.stack).isEmpty();
        assertThat(bonder.getEffectivePower()).isEqualTo(1);
        assertThat(bonder.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Moves a -1/-1 counter from the first target creature onto the second")
    void movesCounterBetweenCreatures() {
        Permanent bonder = addReadyBonder(player1);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bonder.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(bonder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        // Paying {Q} untapped the source.
        assertThat(bonder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can move a counter to a creature an opponent controls")
    void movesCounterToOpponentsCreature() {
        Permanent bonder = addReadyBonder(player1);
        Permanent destination = addCreatureReady(player2, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bonder.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(bonder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing if the first target creature has no counters")
    void noOpWhenSourceHasNoCounters() {
        addReadyBonder(player1);
        Permanent source = addCreatureReady(player1, new WiltLeafCavaliers()); // no counters
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(destination.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("No counter moves if the destination creature leaves before resolution")
    void fizzlesIfDestinationLeaves() {
        Permanent bonder = addReadyBonder(player1);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bonder.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(destination);
        harness.passBothPriorities();

        // No counter was moved off the source.
        assertThat(bonder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it tapped)")
    void cannotActivateWhileUntapped() {
        Permanent bonder = addCreatureReady(player1, new LeechBonder());
        bonder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bonder.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noCounterMovesIfFirstTargetLeaves() {
        addReadyBonder(player1);
        Permanent source = addCreatureReady(player2, new WiltLeafCavaliers());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent bonder = addReadyBonder(player1);
        bonder.setSummoningSick(true);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(bonder.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bonder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void movesPlusCounterFromOpponentsCreature() {
        Permanent bonder = addReadyBonder(player1);
        Permanent source = addCreatureReady(player2, new WiltLeafCavaliers());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        assertThat(bonder.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void controllerChoosesCounterTypeAtResolution() {
        addReadyBonder(player1);
        Permanent source = addCreatureReady(player1, new WiltLeafCavaliers());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.CHARGE, 1);
        Permanent destination = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounters()).isEmpty();
    }

    @Test
    void doesNotRemoveCounterWhenDestinationCannotReceiveIt() {
        Permanent bonder = addReadyBonder(player1);
        Permanent destination = addCreatureReady(player1, new Tatterkite());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bonder.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(bonder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private Permanent addReadyBonder(Player player) {
        Permanent perm = addCreatureReady(player, new LeechBonder());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        perm.tap(); // {Q} requires the source to be tapped
        return perm;
    }

    private Permanent findBonder(Player player) {
        return findPermanent(player, "Leech Bonder");
    }
}
