package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WindingWurm.class)
class WindingWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Winding Wurm at its next upkeep")
    void decliningEchoSacrificesWindingWurm() {
        castAndResolveWindingWurm();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Winding Wurm");
        harness.assertInGraveyard(player1, "Winding Wurm");
    }

    @Test
    @DisplayName("Paying echo keeps Winding Wurm and echo does not trigger again")
    void payingEchoKeepsWindingWurmAndIsOneShot() {
        castAndResolveWindingWurm();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Winding Wurm");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Winding Wurm");
    }

    @Test
    @DisplayName("Echo waits for Winding Wurm's controller's next upkeep")
    void echoTriggersOnlyDuringControllersUpkeep() {
        castAndResolveWindingWurm();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Winding Wurm");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Winding Wurm");
        harness.assertInGraveyard(player1, "Winding Wurm");
    }

    @Test
    @DisplayName("Echo cannot be paid with only colorless mana")
    void echoRequiresGreenMana() {
        castAndResolveWindingWurm();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Winding Wurm");
        harness.assertInGraveyard(player1, "Winding Wurm");
    }

    private void castAndResolveWindingWurm() {
        harness.castFromHand(player1, new WindingWurm(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Winding Wurm");
    }
}
