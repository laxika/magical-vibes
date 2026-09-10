package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodPet;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterHunter.class, BloodPet.class, WindDrake.class})
class RootwaterHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RootwaterHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addCreatureReady(player1, new RootwaterHunter());
        harness.addToBattlefield(player2, new BloodPet());

        UUID targetId = harness.getPermanentId(player2, "Blood Pet");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Blood Pet");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new RootwaterHunter());
        harness.addToBattlefield(player2, new WindDrake());

        UUID targetId = harness.getPermanentId(player2, "Wind Drake");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent hunter = addCreatureReady(player1, new RootwaterHunter());

        harness.activateAbility(player1, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rootwater Hunter");
    }

    @Test
    @DisplayName("Ability taps the creature and cannot be activated twice")
    void abilityTapsAndCannotRepeat() {
        Permanent hunter = addCreatureReady(player1, new RootwaterHunter());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(hunter.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new RootwaterHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }
}
