package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErraticVisionary.class, Forest.class, GrizzlyBears.class})
class ErraticVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card before prompting its controller to discard one")
    void drawsThenDiscards() {
        Permanent visionary = addCreatureReady(player1, new ErraticVisionary());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(visionary.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without the required mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ErraticVisionary());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can discard the card just drawn rather than a card already in hand")
    void canDiscardNewlyDrawnCard() {
        addCreatureReady(player1, new ErraticVisionary());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ErraticVisionary()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Erratic Visionary");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can activate with an empty hand and then discard the drawn card")
    void canActivateWithEmptyHand() {
        addCreatureReady(player1, new ErraticVisionary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Pays the tap cost immediately, before drawing or discarding")
    void tapsBeforeResolution() {
        Permanent visionary = addCreatureReady(player1, new ErraticVisionary());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ErraticVisionary()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(visionary.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.assertInGraveyard(player1, "Erratic Visionary");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent visionary = addCreatureReady(player1, new ErraticVisionary());
        visionary.tap();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ErraticVisionary());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two generic mana cannot pay the blue part of the activation cost")
    void cannotActivateWithoutBlueMana() {
        Permanent visionary = addCreatureReady(player1, new ErraticVisionary());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(visionary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
