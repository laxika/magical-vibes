package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShinenOfLifesRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Shinen of Life's Roar forces all able creatures to block")
    void staticAbilityForcesAllAbleCreaturesToBlock() {
        Permanent attacker = attackingCreature(new ShinenOfLifesRoar());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

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
        Permanent attacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

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
        Permanent attacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

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
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Shinen of Life's Roar");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
