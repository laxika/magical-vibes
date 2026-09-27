package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.setHand(player1, List.of(new QueenMotherRamonda()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
