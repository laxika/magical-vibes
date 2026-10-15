package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BloodPet.class)
class BloodPetTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Blood Pet adds one black mana immediately")
    void activateAbilityAddsBlackManaImmediately() {
        addCreatureReady(player1, new BloodPet());

        harness.activateAbility(player1, 0, null, null);

        // The creature is sacrificed to the graveyard
        harness.assertNotOnBattlefield(player1, "Blood Pet");
        harness.assertInGraveyard(player1, "Blood Pet");

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();

        // One black mana in the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blood Pet can be sacrificed for mana even with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BloodPet());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Blood Pet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped Blood Pet can still be sacrificed for mana")
    void canActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new BloodPet()).tap();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Blood Pet");
        harness.assertInGraveyard(player1, "Blood Pet");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blood Pet's mana can pay for another Blood Pet")
    void producedManaCanPayForCreatureSpell() {
        harness.addToBattlefield(player1, new BloodPet());
        harness.setHand(player1, List.of(new BloodPet()));

        harness.activateAbility(player1, 0, null, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood Pet");
        harness.assertInGraveyard(player1, "Blood Pet");
        harness.assertNotInHand(player1, "Blood Pet");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
