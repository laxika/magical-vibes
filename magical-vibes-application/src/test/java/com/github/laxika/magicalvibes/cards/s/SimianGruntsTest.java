package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimianGruntsTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Simian Grunts at its next upkeep")
    void decliningEchoSacrificesSimianGrunts() {
        castAndResolveSimianGrunts();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Simian Grunts");
        harness.assertInGraveyard(player1, "Simian Grunts");
    }

    @Test
    @DisplayName("Paying echo keeps Simian Grunts and echo does not trigger again")
    void payingEchoKeepsSimianGruntsAndIsOneShot() {
        castAndResolveSimianGrunts();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Simian Grunts");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Simian Grunts");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentUpkeep() {
        castAndResolveSimianGrunts();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Simian Grunts");
    }

    private void castAndResolveSimianGrunts() {
        harness.setHand(player1, List.of(new SimianGrunts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
