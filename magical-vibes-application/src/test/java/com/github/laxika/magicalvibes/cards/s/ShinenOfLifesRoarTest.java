package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
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

@CardUsed({ShinenOfLifesRoar.class, GodosIrregulars.class, MirenTheMoaningWell.class})
class ShinenOfLifesRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Shinen of Life's Roar forces all able creatures to block")
    void staticAbilityForcesAllAbleCreaturesToBlock() {
        Permanent attacker = addCreatureReady(player1, new ShinenOfLifesRoar());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Channel forces all able creatures to block the target this turn")
    void channelForcesAllAbleCreaturesToBlockTarget() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());

        harness.setHand(player1, List.of(new ShinenOfLifesRoar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
        harness.assertInGraveyard(player1, "Shinen of Life's Roar");
    }

    @Test
    @DisplayName("Channel's block requirement wears off at end of turn")
    void channelBlockRequirementWearsOff() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());

        harness.setHand(player1, List.of(new ShinenOfLifesRoar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Channel cannot target a noncreature permanent")
    void channelRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new ShinenOfLifesRoar()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Shinen of Life's Roar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shinen of Life's Roar does not require an unable creature to block")
    void doesNotRequireUnableCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new ShinenOfLifesRoar());
        attacker.setAttacking(true);
        Permanent tappedBlocker = addCreatureReady(player2, new GodosIrregulars());
        tappedBlocker.tap();
        Permanent ableBlocker = addCreatureReady(player2, new GodosIrregulars());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tappedBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Channel can target a creature controlled by an opponent")
    void channelCanTargetOpposingCreature() {
        Permanent attacker = addCreatureReady(player2, new GodosIrregulars());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player1, new GodosIrregulars());

        harness.setHand(player1, List.of(new ShinenOfLifesRoar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Channel requires two green mana")
    void channelRequiresTwoGreenMana() {
        Permanent target = addCreatureReady(player1, new GodosIrregulars());
        harness.setHand(player1, List.of(new ShinenOfLifesRoar()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Shinen of Life's Roar");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }
}
