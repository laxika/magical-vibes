package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShivanRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Shivan Raptor at its next upkeep")
    void decliningEchoSacrificesShivanRaptor() {
        castAndResolveShivanRaptor();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Shivan Raptor");
        harness.assertInGraveyard(player1, "Shivan Raptor");
    }

    @Test
    @DisplayName("Paying echo keeps Shivan Raptor and echo does not trigger again")
    void payingEchoKeepsShivanRaptorAndIsOneShot() {
        castAndResolveShivanRaptor();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Shivan Raptor");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Shivan Raptor");
    }

    private void castAndResolveShivanRaptor() {
        harness.setHand(player1, List.of(new ShivanRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shivan Raptor");
    }
}
