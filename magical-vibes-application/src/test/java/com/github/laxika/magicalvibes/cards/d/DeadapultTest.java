package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.t.ThunderscapeFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deadapult.class, MaggotCarrier.class, ThunderscapeFamiliar.class})
class DeadapultTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a Zombie and deals 2 damage to a player")
    void sacrificesZombieAndDealsDamageToPlayer() {
        addDeadapultAndZombie();
        harness.setLife(player2, 20);

        activateDeadapult(player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maggot Carrier");
        harness.assertInGraveyard(player1, "Maggot Carrier");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can be activated outside the controller's main phase")
    void canActivateOutsideMainPhase() {
        addDeadapultAndZombie();
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        activateDeadapult(player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Maggot Carrier");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrifices a Zombie and deals 2 damage to a creature")
    void sacrificesZombieAndDealsDamageToCreature() {
        addDeadapultAndZombie();
        var targetId = harness.addToBattlefieldAndReturn(player2, new ThunderscapeFamiliar()).getId();

        activateDeadapult(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maggot Carrier");
        harness.assertNotOnBattlefield(player2, "Thunderscape Familiar");
        harness.assertInGraveyard(player2, "Thunderscape Familiar");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Zombie")
    void cannotActivateWithoutZombie() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player1, new ThunderscapeFamiliar());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a Zombie");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Zombie")
    void cannotSacrificeOpponentsZombie() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player2, new MaggotCarrier());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a Zombie");

        harness.assertOnBattlefield(player2, "Maggot Carrier");
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player1, new MaggotCarrier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Maggot Carrier");
        harness.assertNotInGraveyard(player1, "Maggot Carrier");
    }

    private void addDeadapultAndZombie() {
        harness.addToBattlefield(player1, new Deadapult());
        harness.addToBattlefield(player1, new MaggotCarrier());
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void activateDeadapult(UUID targetId) {
        harness.activateAbility(player1, 0, null, targetId);
    }
}
