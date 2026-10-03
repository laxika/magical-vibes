package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjinnOfFoolsFall.class, VedalkenOrrery.class})
class DjinnOfFoolsFallTest extends BaseCardTest {

    @Test
    @DisplayName("Plot exiles the Djinn and allows a free cast on a later turn")
    void plotsAndCastsLater() {
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(djinn));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(djinn.getId());
        assertThat(gd.plottedCardIds).contains(djinn.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, djinn.getId()))
                .hasMessageContaining("on the turn it became plotted");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, djinn.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Djinn of Fool's Fall");
    }

    @Test
    void canCastNormallyWithoutPlotting() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DjinnOfFoolsFall(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Djinn of Fool's Fall");
        assertThat(gd.plottedCardIds).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotPlotWithoutEnoughMana() {
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(djinn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Djinn of Fool's Fall");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void cannotPlotOutsideMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DjinnOfFoolsFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .hasMessageContaining("sorcery speed");

        harness.assertInHand(player1, "Djinn of Fool's Fall");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotCastPlottedCardDuringOpponentsTurnOrUpkeep() {
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(djinn));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, List.of());
        assertThat(gd.stack).isEmpty();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, djinn.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, djinn.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(djinn.getId());
        harness.assertNotOnBattlefield(player1, "Djinn of Fool's Fall");
    }

    @Test
    @CardUsed({DjinnOfFoolsFall.class, VedalkenOrrery.class})
    void flashPermissionDoesNotAllowCastingPlottedCardDuringOpponentsTurn() {
        DjinnOfFoolsFall djinn = new DjinnOfFoolsFall();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(djinn));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, djinn.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(djinn.getId());
        assertThat(gd.stack).isEmpty();
    }
}
