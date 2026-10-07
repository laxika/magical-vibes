package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SpinewoodsPaladin.class)
class SpinewoodsPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when entering the battlefield")
    void gainsThreeLifeOnEnter() {
        harness.castFromHand(player1, new SpinewoodsPaladin(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Plot allows a free cast on a later turn and the ETB trigger resolves")
    void plotsAndCastsLater() {
        SpinewoodsPaladin paladin = new SpinewoodsPaladin();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(paladin));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(paladin.getId());

        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, paladin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertOnBattlefield(player1, "Spinewoods Paladin");
    }

    @Test
    void cannotCastOnTheTurnItWasPlotted() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SpinewoodsPaladin paladin = new SpinewoodsPaladin();
        harness.setHand(player1, List.of(paladin));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThatThrownBy(() -> harness.castFromExile(player1, paladin.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(paladin);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotPlotOutsideMainPhase() {
        SpinewoodsPaladin paladin = new SpinewoodsPaladin();
        harness.setHand(player1, List.of(paladin));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(paladin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotPlotWithoutGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SpinewoodsPaladin paladin = new SpinewoodsPaladin();
        harness.setHand(player1, List.of(paladin));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(paladin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void tramplesOverAnotherPaladin() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new SpinewoodsPaladin());
        Permanent blocker = addCreatureReady(player2, new SpinewoodsPaladin());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Spinewoods Paladin");
        harness.assertInGraveyard(player2, "Spinewoods Paladin");
    }
}
