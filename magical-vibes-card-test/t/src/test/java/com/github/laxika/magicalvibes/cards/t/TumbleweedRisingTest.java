package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TumbleweedRising.class, GrizzlyBears.class, HillGiant.class})
class TumbleweedRisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elemental whose power and toughness equal your greatest creature power")
    void createsElementalEqualToGreatestControlledPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.castFromHand(player1, new TumbleweedRising(), "{1}{G}");
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores creatures controlled by opponents")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.castFromHand(player1, new TumbleweedRising(), "{1}{G}");
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be plotted and cast for free on a later turn")
    void plotsAndCastsLater() {
        TumbleweedRising rising = new TumbleweedRising();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(rising));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.plottedCardIds).contains(rising.getId());
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, rising.getId());
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A 0/0 Elemental token dies when you control no creatures")
    void zeroSizedElementalDies() {

        harness.castFromHand(player1, new TumbleweedRising(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(elemental(player1)).isEmpty();
    }

    @Test
    @DisplayName("Uses the greatest power at resolution rather than when cast")
    void determinesPowerOnResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castFromHand(player1, new TumbleweedRising(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        gd.playerGraveyards.get(player1.getId()).add(giant.getCard());
        harness.passBothPriorities();

        Permanent token = elemental(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast a plotted card on the turn it was plotted")
    void cannotCastOnPlotTurn() {
        TumbleweedRising rising = new TumbleweedRising();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(rising));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, rising.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("turn it became plotted");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plotting requires sorcery timing")
    void cannotPlotDuringUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new TumbleweedRising()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.plottedCardIds).isEmpty();
    }

    @Test
    @DisplayName("Casting a plotted card on a later turn still requires sorcery timing")
    void cannotCastPlottedCardDuringUpkeep() {
        TumbleweedRising rising = new TumbleweedRising();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(rising));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        gd.turnNumber++;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, rising.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    private Optional<Permanent> elemental(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .findFirst();
    }
}
