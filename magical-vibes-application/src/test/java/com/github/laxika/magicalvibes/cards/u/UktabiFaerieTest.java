package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HorribleHordes;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UktabiFaerie.class, HorribleHordes.class, IronTuskElephant.class})
class UktabiFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysTargetArtifact() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new HorribleHordes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Horrible Hordes");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Horrible Hordes");
        harness.assertInGraveyard(player2, "Horrible Hordes");
    }

    @Test
    @DisplayName("Uktabi Faerie is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new HorribleHordes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Horrible Hordes");
        harness.activateAbility(player1, 0, 0, null, targetId);

        harness.assertNotOnBattlefield(player1, "Uktabi Faerie");
        harness.assertInGraveyard(player1, "Uktabi Faerie");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Iron Tusk Elephant");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Rejecting a non-artifact target does not pay the sacrifice cost")
    void invalidTargetDoesNotPaySacrificeCost() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Iron Tusk Elephant");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Uktabi Faerie");
        harness.assertNotInGraveyard(player1, "Uktabi Faerie");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its controller")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player1, new HorribleHordes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Horrible Hordes");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Horrible Hordes");
        harness.assertInGraveyard(player1, "Horrible Hordes");
        harness.assertInGraveyard(player1, "Uktabi Faerie");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        var faerie = harness.addToBattlefieldAndReturn(player1, new UktabiFaerie());
        faerie.tap();
        faerie.setSummoningSick(true);
        harness.addToBattlefield(player2, new HorribleHordes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Horrible Hordes");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Uktabi Faerie");
        harness.assertNotOnBattlefield(player2, "Horrible Hordes");
        harness.assertInGraveyard(player2, "Horrible Hordes");
    }

    @Test
    @DisplayName("Four colorless mana cannot pay the activation's green requirement")
    void missingGreenManaDoesNotSacrificeFaerie() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new HorribleHordes());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Horrible Hordes");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Uktabi Faerie");
        harness.assertNotInGraveyard(player1, "Uktabi Faerie");
        harness.assertOnBattlefield(player2, "Horrible Hordes");
        harness.assertNotInGraveyard(player2, "Horrible Hordes");
    }
}
