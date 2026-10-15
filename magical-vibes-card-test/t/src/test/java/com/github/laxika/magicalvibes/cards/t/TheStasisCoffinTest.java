package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheStasisCoffin.class, Shock.class, GrizzlyBears.class})
class TheStasisCoffinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling The Stasis Coffin grants protection from everything")
    void exilesAndProtectsController() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Stasis Coffin");
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");

        harness.setLife(player1, 20);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, java.util.List.of(0));
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Protection from everything ends at the controller's next turn")
    void protectionEndsAtNextTurn() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Exiling is paid immediately, but protection waits for resolution")
    void exileIsCostAndProtectionUsesStack() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "The Stasis Coffin");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("The Stasis Coffin"));
        harness.assertNotInGraveyard(player1, "The Stasis Coffin");
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");
    }

    @Test
    @DisplayName("Protection makes an already-cast spell's player target illegal")
    void protectionStopsSpellAlreadyOnStack() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Activation requires two mana and does not exile the Coffin without it")
    void insufficientManaDoesNotPayExileCost() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "The Stasis Coffin")).isNotNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A tapped Coffin cannot pay its tap cost")
    void tappedCoffinCannotActivate() {
        harness.addToBattlefield(player1, new TheStasisCoffin());
        findPermanent(player1, "The Stasis Coffin").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "The Stasis Coffin")).isNotNull();
        assertThat(gd.exiledCards).isEmpty();
    }
}
