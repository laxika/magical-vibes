package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeneyeAven.class, FugitiveWizard.class})
class KeeneyeAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {2} discards Keeneye Aven and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new KeeneyeAven()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Keeneye Aven");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling cannot be activated without paying {2}")
    void cyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new KeeneyeAven()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Keeneye Aven");
        harness.assertNotInGraveyard(player1, "Keeneye Aven");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards immediately and accepts colored mana for its generic cost")
    void cyclingPaysCostsBeforeDrawing() {
        harness.setHand(player1, List.of(new KeeneyeAven()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Keeneye Aven");
        harness.assertInGraveyard(player1, "Keeneye Aven");
        harness.assertNotInHand(player1, "Fugitive Wizard");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Fugitive Wizard");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling is available during an opponent's upkeep")
    void cyclingDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new KeeneyeAven()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Keeneye Aven");
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertNotInHand(player2, "Fugitive Wizard");
        assertThat(gd.stack).isEmpty();
    }
}
