package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QueenMotherRamonda.class, GrizzlyBears.class, HillGiant.class})
class QueenMotherRamondaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and makes its controller the monarch")
    void entersAndMakesControllerMonarch() {
        castQueenMotherRamonda();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("While its controller is the monarch, creatures with power 2 or less cannot attack them")
    void monarchRestrictionStopsSmallCreatures() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        gd.monarchPlayerId = player2.getId();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction is inactive when its controller is not the monarch")
    void restrictionIsInactiveWhenControllerIsNotMonarch() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        gd.monarchPlayerId = player1.getId();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Creatures with power 3 or greater can attack the monarch")
    void largerCreaturesCanAttackTheMonarch() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        gd.monarchPlayerId = player2.getId();
        addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(0));
    }

    private void castQueenMotherRamonda() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new QueenMotherRamonda(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void enteringTakesMonarchyFromOpponent() {
        gd.monarchPlayerId = player2.getId();

        castQueenMotherRamonda();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void restrictionIsInactiveWhenThereIsNoMonarch() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
    }

    @Test
    void increasedPowerAllowsNormallySmallCreatureToAttack() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        gd.monarchPlayerId = player2.getId();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setPowerModifier(1);

        declareAttackers(player1, List.of(0));
    }

    @Test
    void reducedPowerStopsNormallyLargeCreatureFromAttacking() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        gd.monarchPlayerId = player2.getId();
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setPowerModifier(-1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void restrictionTracksChangesInMonarch() {
        harness.addToBattlefield(player2, new QueenMotherRamonda());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gd.monarchPlayerId = player2.getId();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();

        gd.monarchPlayerId = player1.getId();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();

        gd.monarchPlayerId = player2.getId();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
    }
}
