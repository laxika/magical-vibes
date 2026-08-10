package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolOfPiranhaTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {1}{U} sacrifices School of Piranha")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new SchoolOfPiranha());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "School of Piranha");
        harness.assertInGraveyard(player1, "School of Piranha");
    }

    @Test
    @DisplayName("Paying {1}{U} keeps School of Piranha on the battlefield")
    void payKeeps() {
        harness.addToBattlefield(player1, new SchoolOfPiranha());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "School of Piranha");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices School of Piranha")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new SchoolOfPiranha());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "School of Piranha");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SchoolOfPiranha());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "School of Piranha");
    }
}
