package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
