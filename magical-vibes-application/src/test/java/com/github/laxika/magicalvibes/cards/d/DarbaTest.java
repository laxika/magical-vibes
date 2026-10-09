package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Darba.class)
class DarbaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying upkeep keeps Darba on the battlefield")
    void payingUpkeepKeepsDarba() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        harness.assertOnBattlefield(player1, "Darba");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining upkeep sacrifices Darba")
    void decliningUpkeepSacrificesDarba() {
        harness.addToBattlefieldAndReturn(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, false));

        harness.assertNotOnBattlefield(player1, "Darba");
        harness.assertInGraveyard(player1, "Darba");
    }

    @Test
    @DisplayName("Accepting upkeep with only one green mana still sacrifices Darba")
    void acceptingWithInsufficientManaSacrificesDarba() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        harness.assertNotOnBattlefield(player1, "Darba");
        harness.assertInGraveyard(player1, "Darba");
    }

    @Test
    @DisplayName("Darba's upkeep trigger does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Darba");
    }

    @Test
    @DisplayName("Declining upkeep with enough mana sacrifices Darba without spending mana")
    void decliningWithEnoughManaDoesNotSpendMana() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, false));

        harness.assertNotOnBattlefield(player1, "Darba");
        harness.assertInGraveyard(player1, "Darba");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-green mana cannot pay Darba's upkeep")
    void wrongManaColorsCannotPayUpkeep() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        harness.assertNotOnBattlefield(player1, "Darba");
        harness.assertInGraveyard(player1, "Darba");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's mana cannot pay Darba's upkeep")
    void opponentsManaCannotPayUpkeep() {
        harness.addToBattlefield(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        harness.assertNotOnBattlefield(player1, "Darba");
        harness.assertInGraveyard(player1, "Darba");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
