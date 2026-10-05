package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PalladiaMors.class)
class PalladiaMorsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R}{G}{W} during upkeep keeps Palladia-Mors on the battlefield")
    void payingUpkeepCostKeepsPalladiaMors() {
        addCreatureReady(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Palladia-Mors")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Palladia-Mors")
    void decliningUpkeepCostSacrificesPalladiaMors() {
        addCreatureReady(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Palladia-Mors")).isZero();
    }

    @Test
    @DisplayName("Accepting the upkeep payment without enough mana sacrifices Palladia-Mors")
    void acceptingUpkeepCostWithoutEnoughManaSacrificesPalladiaMors() {
        addCreatureReady(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Palladia-Mors")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Palladia-Mors does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new PalladiaMors());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Palladia-Mors");
    }

    @Test
    @DisplayName("Three mana of the wrong colors cannot pay the upkeep cost")
    void wrongColorsCannotPayUpkeepCost() {
        addCreatureReady(player1, new PalladiaMors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Palladia-Mors");
        harness.assertInGraveyard(player1, "Palladia-Mors");
    }

    @Test
    @DisplayName("An unpaid upkeep sacrifices Palladia-Mors controlled by the second player")
    void secondPlayersUnpaidUpkeepSacrificesPalladiaMors() {
        addCreatureReady(player2, new PalladiaMors());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Palladia-Mors");
        harness.assertInGraveyard(player2, "Palladia-Mors");
        harness.assertNotInGraveyard(player1, "Palladia-Mors");
    }
}
