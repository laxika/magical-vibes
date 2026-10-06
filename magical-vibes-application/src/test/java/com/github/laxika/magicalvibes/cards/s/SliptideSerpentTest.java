package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SliptideSerpent.class)
class SliptideSerpentTest extends BaseCardTest {

    @Test
    void activateAbilityReturnsSliptideSerpentToItsOwnersHand() {
        harness.addToBattlefield(player1, new SliptideSerpent());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sliptide Serpent");
        harness.assertNotOnBattlefield(player1, "Sliptide Serpent");
    }

    @Test
    void canActivateWhileTappedAndSummoningSickWithGenericAndBlueMana() {
        var serpent = harness.addToBattlefieldAndReturn(player1, new SliptideSerpent());
        serpent.setTapped(true);
        serpent.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertOnBattlefield(player1, "Sliptide Serpent");
        harness.assertNotInHand(player1, "Sliptide Serpent");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sliptide Serpent");
        harness.assertNotOnBattlefield(player1, "Sliptide Serpent");
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new SliptideSerpent());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sliptide Serpent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithLessThanFourMana() {
        harness.addToBattlefield(player1, new SliptideSerpent());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sliptide Serpent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsToOwnerRatherThanController() {
        var serpent = new SliptideSerpent();
        serpent.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, serpent);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Sliptide Serpent");
        harness.assertNotInHand(player1, "Sliptide Serpent");
        harness.assertNotOnBattlefield(player1, "Sliptide Serpent");
    }

    @Test
    void secondActivationDoesNothingAfterSourceHasAlreadyReturned() {
        harness.addToBattlefield(player1, new SliptideSerpent());
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sliptide Serpent");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Sliptide Serpent"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
