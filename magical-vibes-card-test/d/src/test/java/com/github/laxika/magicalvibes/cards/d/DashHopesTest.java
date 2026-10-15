package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DashHopes.class, KavuPredator.class, EverybodyLives.class})
class DashHopesTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when no player pays 5 life")
    void countersTargetSpellWhenNoPlayerPays() {
        castTargetSpell();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    @Test
    @DisplayName("Any player may pay 5 life to counter Dash Hopes")
    void anyPlayerMayPayToCounterDashHopes() {
        castTargetSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("The spell's controller may pay 5 life to counter Dash Hopes")
    void spellControllerMayPayToCounterDashHopes() {
        castTargetSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A player with less than 5 life cannot pay")
    void playerWithInsufficientLifeCannotPay() {
        harness.setLife(player1, 4);
        harness.setLife(player2, 4);
        castTargetSpell();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    @Test
    @DisplayName("Remaining players may still pay after Dash Hopes has been countered")
    void bothPlayersMayPay() {
        castTargetSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Dash Hopes");
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Kavu Predator");
    }

    @Test
    @DisplayName("Skips an ineligible player but offers payment to the next player")
    void skipsPlayerWithInsufficientLife() {
        harness.setLife(player1, 4);
        castTargetSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    @Test
    @DisplayName("Players who cannot lose life cannot pay to counter Dash Hopes")
    void cannotPayWhenLifeLossIsProhibited() {
        harness.castFromHand(player1, new EverybodyLives(), "{1}{W}");
        harness.passBothPriorities();
        castTargetSpell();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice).isFalse();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    private void castTargetSpell() {
        KavuPredator predator = new KavuPredator();
        harness.castFromHand(player1, predator, "{1}{G}");

        harness.setHand(player2, List.of(new DashHopes()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, predator.getId());
    }
}
