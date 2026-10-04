package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
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

@DisplayName("Foul Familiar")
@CardUsed({FoulFamiliar.class, BalduvianBears.class})
class FoulFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Foul Familiar cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new FoulFamiliar());

        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("{B}, Pay 1 life returns Foul Familiar to owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new FoulFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Foul Familiar");
        harness.assertNotOnBattlefield(player1, "Foul Familiar");
    }

    @Test
    @DisplayName("returns to its owner's hand when controlled by another player")
    void activateAbilityReturnsToOwnersHand() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player2, new FoulFamiliar());
        gd.stolenCreatures.put(familiar.getId(), player1.getId());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Foul Familiar");
        harness.assertNotInHand(player2, "Foul Familiar");
        harness.assertNotOnBattlefield(player2, "Foul Familiar");
    }

    @Test
    @DisplayName("Cannot activate with insufficient life")
    void cannotActivateWithInsufficientLife() {
        harness.addToBattlefield(player1, new FoulFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new FoulFamiliar());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Foul Familiar");
    }
    @Test
    @DisplayName("Life is paid on activation, before Foul Familiar returns on resolution")
    void paysLifeBeforeResolution() {
        harness.addToBattlefield(player1, new FoulFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player1, "Foul Familiar");
        harness.assertNotInHand(player1, "Foul Familiar");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Foul Familiar");
        harness.assertNotOnBattlefield(player1, "Foul Familiar");
    }

    @Test
    @DisplayName("A tapped Foul Familiar can activate the turn it enters")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar());
        familiar.tap();
        familiar.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Foul Familiar");
        harness.assertNotOnBattlefield(player1, "Foul Familiar");
    }

    @Test
    @DisplayName("Two activations pay twice but return the source only once")
    void multipleActivationsDoNotReturnAnAbsentSource() {
        harness.addToBattlefield(player1, new FoulFamiliar());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Foul Familiar");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof FoulFamiliar)
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
