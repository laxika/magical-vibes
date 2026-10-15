package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeJuggler.class})
class BladeJugglerTest extends BaseCardTest {

    @Test
    @DisplayName("When Blade Juggler enters, it deals 1 damage to you and you draw a card")
    void entersDealsDamageAndDrawsCard() {
        harness.setHand(player1, List.of(new BladeJuggler()));
        harness.setLibrary(player1, List.of(new BladeJuggler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Blade Juggler");
        harness.assertOnBattlefield(player1, "Blade Juggler");
    }

    @Test
    @DisplayName("Spectacle casts Blade Juggler for {2}{B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new BladeJuggler()));
        harness.setLibrary(player1, List.of(new BladeJuggler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Blade Juggler");
        harness.assertOnBattlefield(player1, "Blade Juggler");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new BladeJuggler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster losing life does not enable spectacle")
    void ownLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new BladeJuggler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Blade Juggler");
        harness.assertNotOnBattlefield(player1, "Blade Juggler");
    }

    @Test
    @DisplayName("Spectacle remains available after the opponent regains the lost life")
    void spectacleDoesNotRequireNetLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BladeJuggler()));
        harness.setLibrary(player1, List.of(new BladeJuggler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Blade Juggler");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        harness.assertInHand(player1, "Blade Juggler");
    }

    @Test
    @DisplayName("An entry without casting triggers for its controller even if Blade Juggler dies")
    void entryTriggerSurvivesSourceAndUsesItsController() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new BladeJuggler()));
        var juggler = harness.enterBattlefieldAndReturn(player2, new BladeJuggler());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        juggler.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Blade Juggler");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Blade Juggler");
        harness.assertNotOnBattlefield(player2, "Blade Juggler");
    }
}
