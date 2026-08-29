package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EndlessWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep without an enchantment sacrifices Endless Wurm without prompting")
    void upkeepWithoutEnchantmentSacrificesWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endless Wurm");
        harness.assertInGraveyard(player1, "Endless Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing an enchantment keeps Endless Wurm")
    void sacrificingEnchantmentKeepsWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());
        harness.addToBattlefield(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Glorious Anthem").getId());

        harness.assertOnBattlefield(player1, "Endless Wurm");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining to sacrifice an enchantment sacrifices Endless Wurm")
    void decliningSacrificesWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());
        harness.addToBattlefield(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Endless Wurm");
        harness.assertInGraveyard(player1, "Endless Wurm");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }
}
