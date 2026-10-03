package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClawsOfGix.class, TormodsCrypt.class, Desert.class})
class ClawsOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another permanent gains 1 life")
    void sacrificeAnotherPermanentGainsOneLife() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, crypt.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Tormod's Crypt");
        harness.assertOnBattlefield(player1, "Claws of Gix");
    }

    @Test
    @DisplayName("Claws of Gix can be sacrificed to pay its own ability")
    void sacrificeSourceGainsOneLife() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Claws of Gix");
    }

    @Test
    @DisplayName("Cannot be activated without generic mana")
    void cannotActivateWithoutGenericMana() {
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(claws);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(claws.getCard());
    }

    @Test
    @DisplayName("Only the controller's permanents can be sacrificed")
    void onlyControllerPermanentsCanBeSacrificed() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent ownCrypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        Permanent opponentCrypt = harness.addToBattlefieldAndReturn(player2, new TormodsCrypt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .contains(ownCrypt.getId())
                .doesNotContain(opponentCrypt.getId());

        harness.handlePermanentChosen(player1, ownCrypt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tormod's Crypt");
        harness.assertOnBattlefield(player2, "Tormod's Crypt");
    }

    @Test
    @DisplayName("A tapped land can be sacrificed, and life is gained only on resolution")
    void sacrificeLandWithColoredManaPaysCostBeforeResolution() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Desert());
        land.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertInGraveyard(player1, "Desert");
        harness.assertNotOnBattlefield(player1, "Desert");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Claws of Gix");
    }

    @Test
    @DisplayName("A tapped Claws of Gix can activate repeatedly without untapping")
    void tappedSourceCanActivateRepeatedly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());
        source.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(source.isTapped()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 21);
        harness.assertNotOnBattlefield(player1, "Claws of Gix");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Claws of Gix can activate during its opponent's turn and gains life for its controller")
    void nonActiveControllerCanActivate() {
        harness.addToBattlefield(player2, new ClawsOfGix());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        harness.assertInGraveyard(player2, "Claws of Gix");
    }
}
