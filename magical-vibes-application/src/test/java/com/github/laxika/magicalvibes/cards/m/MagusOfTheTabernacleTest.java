package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheTabernacle.class, GrizzlyBears.class})
class MagusOfTheTabernacleTest extends BaseCardTest {

    private Permanent addMagus(Player controller) {
        Permanent magus = new Permanent(new MagusOfTheTabernacle());
        gd.playerBattlefields.get(controller.getId()).add(magus);
        return magus;
    }

    private Permanent addBears(Player controller) {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(controller.getId()).add(bears);
        return bears;
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices the Magus itself")
    void decliningPaymentSacrificesMagus() {
        addMagus(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magus of the Tabernacle");
        harness.assertInGraveyard(player1, "Magus of the Tabernacle");
    }

    @Test
    @DisplayName("Paying {1} keeps the Magus on the battlefield")
    void payingKeepsMagus() {
        addMagus(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Magus of the Tabernacle");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Magus taxes other creatures")
    void taxesOtherCreatures() {
        addMagus(player1);
        Permanent bears = addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
    }
}
