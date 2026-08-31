package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, MuerraTrashTactician.class, RaccoonRallier.class, Shock.class})
class MuerraTrashTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana for each Raccoon at the beginning of the first main phase")
    void addsManaForEachRaccoon() {
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.addToBattlefield(player1, new RaccoonRallier());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains three life when its controller expends four")
    void gainsLifeWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        castShocks(4);

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Exiles the top two cards with permission to play them when its controller expends eight")
    void exilesTopTwoCardsWhenControllerExpendsEight() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.RED, 8);

        castShocks(8);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest, bears);
        assertThat(gd.exilePlayPermissions.get(forest.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        harness.assertLife(player1, 23);
    }

    private void castShocks(int count) {
        for (int i = 0; i < count; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
