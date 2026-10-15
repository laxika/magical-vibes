package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TectonicFiend.class})
class TectonicFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Tectonic Fiend at its next upkeep")
    void decliningEchoSacrificesTectonicFiend() {
        castAndResolveTectonicFiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tectonic Fiend");
        harness.assertInGraveyard(player1, "Tectonic Fiend");
    }

    @Test
    @DisplayName("Paying echo keeps Tectonic Fiend and echo does not trigger again")
    void payingEchoKeepsTectonicFiendAndIsOneShot() {
        castAndResolveTectonicFiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        addEchoMana();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tectonic Fiend");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tectonic Fiend");
    }

    @Test
    @DisplayName("Tectonic Fiend must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new TectonicFiend());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Tectonic Fiend is not required to attack while summoning sick")
    void mustAttackOnlyWhenAble() {
        harness.addToBattlefieldAndReturn(player1, new TectonicFiend());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Echo does not create an enters-the-battlefield trigger")
    void enteringDoesNotCreateEchoTrigger() {
        harness.enterBattlefieldAndReturn(player1, new TectonicFiend());

        harness.assertOnBattlefield(player1, "Tectonic Fiend");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Echo waits through the opponent's upkeep")
    void echoWaitsForControllersUpkeep() {
        castAndResolveTectonicFiend();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tectonic Fiend");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Tectonic Fiend");
    }

    @Test
    @DisplayName("A tapped Tectonic Fiend is not required to attack")
    void tappedFiendNeedNotAttack() {
        addCreatureReady(player1, new TectonicFiend()).tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Declaring Tectonic Fiend as an attacker satisfies its requirement")
    void fiendCanAttack() {
        addCreatureReady(player1, new TectonicFiend());

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }

    private void castAndResolveTectonicFiend() {
        harness.castFromHand(player1, new TectonicFiend(), "{4}{R}{R}");
        resolveAllTriggers();
    }

    private void addEchoMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
