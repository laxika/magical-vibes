package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingerbackTerror.class, GrizzlyBears.class})
class StingerbackTerrorTest extends BaseCardTest {
    @Test
    void opponentsHandDoesNotReduceItsStats() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new StingerbackTerror(), new StingerbackTerror()));
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new StingerbackTerror());

        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, terror)).isEqualTo(7);
    }

    @Test
    void diesWhenControllerHasSevenCardsInHand() {
        StingerbackTerror terror = new StingerbackTerror();
        harness.setHand(player1, List.of(new StingerbackTerror(), new StingerbackTerror(),
                new StingerbackTerror(), new StingerbackTerror(), new StingerbackTerror(),
                new StingerbackTerror(), new StingerbackTerror()));
        harness.addToBattlefield(player1, terror);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Stingerback Terror");
        harness.assertInGraveyard(player1, "Stingerback Terror");
    }

    @Test
    void plottedCardCanBeCastForFreeOnALaterTurn() {
        StingerbackTerror terror = new StingerbackTerror();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(terror));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, terror.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of());
        assertThatThrownBy(() -> harness.castFromExile(player1, terror.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.castFromExile(player1, terror.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Stingerback Terror");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(terror);
    }

    @Test
    void cannotPlotOutsideAMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        StingerbackTerror terror = new StingerbackTerror();
        harness.setHand(player1, List.of(terror));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(terror);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(terror);
    }

    @Test
    @DisplayName("Gets -1/-1 for each card in its controller's hand")
    void getsMinusOneMinusOnePerCardInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent terror = addCreatureReady(player1, new StingerbackTerror());

        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, terror)).isEqualTo(4);

        gd.playerHands.get(player1.getId()).removeLast();

        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, terror)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can be plotted for {2}{R}")
    void canBePlottedForItsPlotCost() {
        StingerbackTerror terror = new StingerbackTerror();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(terror));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(terror);
        assertThat(gd.plottedCardIds).contains(terror.getId());
    }
}
