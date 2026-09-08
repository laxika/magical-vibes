package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PalladiaMorsTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {R}{G}{W} sacrifices Palladia-Mors")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Palladia-Mors");
        harness.assertInGraveyard(player1, "Palladia-Mors");
    }

    @Test
    @DisplayName("Paying {R}{G}{W} keeps Palladia-Mors on the battlefield")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Palladia-Mors");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Accepting without all required mana sacrifices Palladia-Mors")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Palladia-Mors");
    }

    @Test
    @DisplayName("Palladia-Mors does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PalladiaMors());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Palladia-Mors");
    }
}
