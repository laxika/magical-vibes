package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinAssault;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.t.TrumpetingArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinAssault.class, GrizzlyBears.class, HillGiant.class, Okk.class, ShivanDragon.class,
        TrumpetingArmodon.class})
class OkkTest extends BaseCardTest {

    // --- Attacking ---

    @Test
    @DisplayName("Okk can't attack alone")
    void cannotAttackAlone() {
        addCreatureReady(player1, new Okk());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can't attack when the other attacker has less power")
    void cannotAttackWithWeakerAlly() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new HillGiant()); // 3/3 < 4/4

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can't attack when a stronger ally stays home")
    void cannotAttackWhenStrongerAllyDoesNotAttack() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new ShivanDragon()); // 5/5, but it is not declared as an attacker

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can attack when a creature with greater power also attacks")
    void canAttackWithStrongerAlly() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new ShivanDragon()); // 5/5 > 4/4

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Okk's attack restriction disappears when it loses all abilities")
    void attackRestrictionDisappearsWhenItLosesAllAbilities() {
        Permanent okk = addCreatureReady(player1, new Okk());
        okk.setLosesAllAbilitiesUntilEndOfTurn(true);

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Okk that lost its abilities still attacks when a must-attack effect requires it")
    void lostAttackRestrictionStillSatisfiesMustAttackRequirement() {
        Permanent okk = addCreatureReady(player1, new Okk());
        okk.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefield(player1, new GoblinAssault());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Okk can't attack with a creature of equal power")
    void cannotAttackWithEqualPowerAlly() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new Okk());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can't attack when the other attacker has equal power")
    void cannotAttackWithEqualPowerAllyUpstreamReview() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new Okk()); // 4/4 is not greater than 4/4

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    // --- Blocking ---

    @Test
    @DisplayName("Okk can't block alone")
    void cannotBlockAlone() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can't block when the other blocker has less power")
    void cannotBlockWithWeakerAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new HillGiant()); // 3/3 < 4/4
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can't block when a stronger ally stays home")
    void cannotBlockWhenStrongerAllyDoesNotBlock() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new ShivanDragon()); // 5/5, but it is not declared as a blocker
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can block when a creature with greater power also blocks")
    void canBlockWithStrongerAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new ShivanDragon()); // 5/5 > 4/4
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Okk can block when a stronger ally blocks a different attacker")
    void canBlockWithStrongerAllyOnDifferentAttacker() {
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new ShivanDragon());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Okk's block restriction disappears when it loses all abilities")
    void blockRestrictionDisappearsWhenItLosesAllAbilities() {
        addReadyAttacker(player1);
        Permanent okk = addCreatureReady(player2, new Okk());
        okk.setLosesAllAbilitiesUntilEndOfTurn(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Okk that lost its abilities still blocks when a must-block effect requires it")
    void lostBlockRestrictionStillSatisfiesMustBlockRequirement() {
        Permanent armodon = addCreatureReady(player1, new TrumpetingArmodon());
        Permanent okk = addCreatureReady(player2, new Okk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, okk.getId());
        harness.passBothPriorities();
        okk.setLosesAllAbilitiesUntilEndOfTurn(true);
        armodon.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Okk can't block with a creature of equal power")
    void cannotBlockWithEqualPowerAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new Okk());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can't block when the other blocker has equal power")
    void cannotBlockWithEqualPowerAllyUpstreamReview() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new Okk()); // 4/4 is not greater than 4/4
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }
}
