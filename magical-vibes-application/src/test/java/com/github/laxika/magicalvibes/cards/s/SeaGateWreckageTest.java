package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGateWreckage.class, Wastes.class})
class SeaGateWreckageTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        Permanent wreckage = addWreckage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(wreckage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        Permanent wreckage = addWreckage();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Wastes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(wreckage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the draw ability while the controller holds a card")
    void cannotDrawWithCardsInHand() {
        Permanent wreckage = addWreckage();
        harness.setHand(player1, List.of(new Wastes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");

        assertThat(wreckage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires a colorless mana for the draw ability")
    void requiresColorlessMana() {
        addWreckage();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw ability still resolves after a card enters the controller's hand")
    void drawsAfterHandBecomesNonempty() {
        addWreckage();
        harness.setHand(player1, List.of());
        Wastes drawnCard = new Wastes();
        Wastes cardInHand = new Wastes();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(cardInHand));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand, drawnCard);
    }

    @Test
    @DisplayName("An opponent's nonempty hand does not prevent drawing")
    void opponentHandDoesNotPreventDrawing() {
        addWreckage();
        harness.setHand(player1, List.of());
        Wastes opponentCard = new Wastes();
        Wastes drawnCard = new Wastes();
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("A newly entered noncreature land can activate its draw ability")
    void newlyEnteredLandCanDraw() {
        Permanent wreckage = harness.addToBattlefieldAndReturn(player1, new SeaGateWreckage());
        harness.setHand(player1, List.of());
        Wastes drawnCard = new Wastes();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(wreckage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The draw ability requires the full three mana even when colorless is available")
    void cannotDrawWithInsufficientTotalMana() {
        Permanent wreckage = addWreckage();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wreckage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped Wreckage cannot activate either ability")
    void tappedLandCannotActivate() {
        Permanent wreckage = addWreckage();
        wreckage.setTapped(true);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private Permanent addWreckage() {
        Permanent wreckage = harness.addToBattlefieldAndReturn(player1, new SeaGateWreckage());
        wreckage.setSummoningSick(false);
        return wreckage;
    }
}
