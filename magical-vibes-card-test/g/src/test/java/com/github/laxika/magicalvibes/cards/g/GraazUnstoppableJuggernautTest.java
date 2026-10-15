package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraazUnstoppableJuggernaut.class, GrizzlyBears.class, WallOfWood.class})
class GraazUnstoppableJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control become 5/3 Juggernauts")
    void otherCreaturesBecomeFiveThreeJuggernauts() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, ownCreature).grantedSubtypes())
                .contains(CardSubtype.JUGGERNAUT);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.JUGGERNAUT);
    }

    @Test
    @DisplayName("Juggernauts you control must attack each combat if able")
    void juggernautsYouControlMustAttack() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Juggernauts you control can't be blocked by Walls")
    void juggernautsYouControlCannotBeBlockedByWalls() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent nonWall = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int wallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int nonWallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(nonWall);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(wallIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(nonWallIndex, attackerIndex)));
        assertThat(nonWall.isBlocking()).isTrue();
    }

    @Test
    void transformedCreatureMustAttackEvenWhenGraazAttacks() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void defenderAndTappedCreaturesAreNotRequiredToAttack() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut()).tap();
        addCreatureReady(player1, new GrizzlyBears()).tap();
        Permanent wall = addCreatureReady(player1, new WallOfWood());

        declareAttackers(List.of());

        assertThat(wall.isAttacking()).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, wall, CardSubtype.WALL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, wall, CardSubtype.JUGGERNAUT)).isTrue();
    }

    @Test
    void countersModifyTheGrantedBaseStats() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.BEAR)).isTrue();
    }

    @Test
    void effectsEndWhenGraazLeavesBattlefield() {
        Permanent graaz = addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        gd.playerBattlefields.get(player1.getId()).remove(graaz);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.JUGGERNAUT)).isFalse();
        declareAttackers(List.of());
    }

    @Test
    void graazItselfCannotBeBlockedByWalls() {
        Permanent graaz = addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        graaz.setAttacking(true);
        addCreatureReady(player2, new WallOfWood());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({DressDown.class})
    void losingAbilitiesPreservesTheEntireTypeAndBaseStatsEffect() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DressDown());

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.JUGGERNAUT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @CardUsed({DressDown.class})
    void losingAbilitiesEndsTheAttackRequirementAndWallRestriction() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DressDown());
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        declareAttackers(List.of());
        creature.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
