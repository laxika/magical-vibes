package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarrelingAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants trample to the target")
    void grantsTrample() {
        Permanent bear = readyCreature(player1);
        castBarrelingAttack(bear);

        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent bear = readyCreature(player1);
        castBarrelingAttack(bear);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        readyCreature(player1);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BarrelingAttack()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Becoming blocked by one creature gives the target +1/+1")
    void oneBlockerGivesPlusOnePlusOne() {
        Permanent bear = readyCreature(player1);
        readyCreature(player2);
        castBarrelingAttack(bear);

        bear.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures gives the target +2/+2")
    void twoBlockersGivePlusTwoPlusTwo() {
        Permanent bear = readyCreature(player1);
        readyCreature(player2);
        readyCreature(player2);
        castBarrelingAttack(bear);

        bear.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Staying unblocked gives no boost")
    void unblockedGivesNoBoost() {
        Permanent bear = readyCreature(player1);
        castBarrelingAttack(bear);

        bear.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A creature that was not the target gets no boost when blocked")
    void untargetedCreatureGetsNoBoost() {
        Permanent target = readyCreature(player1);
        Permanent other = readyCreature(player1);
        readyCreature(player2);
        castBarrelingAttack(target);

        other.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
    }

    private void castBarrelingAttack(Permanent target) {
        harness.setHand(player1, List.of(new BarrelingAttack()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent readyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
