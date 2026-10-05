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

    @Test
    void countsOnlyControlledRaccoonsWhenManaTriggerResolves() {
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RaccoonRallier());
        advanceToPrecombatMain(player1);
        harness.addToBattlefield(player1, new RaccoonRallier());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void gainsLifeWhenOneSpellCrossesFourAndDoesNotTriggerAgain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new RaccoonRallier(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 6);

        castShocks(3);
        harness.assertLife(player1, 20);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        castShocks(1);
        harness.assertLife(player1, 23);
    }

    @Test
    void opponentsManaSpendingDoesNotTriggerMuerra() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castInstant(player2, 0, player1.getId());
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    void canPlayExiledLandAndCastExiledCreatureByPayingItsCost() {
        Forest forest = new Forest();
        RaccoonRallier rallier = new RaccoonRallier();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(forest, rallier));
        harness.addMana(player1, ManaColor.RED, 8);
        castShocks(8);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.castFromExile(player1, forest.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, rallier.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raccoon Rallier");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player1, 23);
    }

    @Test
    void exilesOnlyAvailableCardFromShortLibrary() {
        Forest forest = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 8);

        castShocks(8);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exilePermissionLastsThroughNextTurnAndThenExpires() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MuerraTrashTactician());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(first, second, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 8);
        castShocks(8);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKeys(first.getId(), second.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKeys(first.getId(), second.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void spendingBeforeMuerraEntersStillCountsButDoesNotTriggerRetroactively() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        castShocks(4);
        harness.addToBattlefield(player1, new MuerraTrashTactician());

        castShocks(1);

        harness.assertLife(player1, 20);
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
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
