package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaugrinTriome.class})
class RaugrinTriomeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new RaugrinTriome()));

        harness.playLand(player1, 0);

        Permanent triome = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(triome.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BLUE", "RED", "WHITE"})
    @DisplayName("Mana ability adds the chosen color")
    void addsChosenManaColor(String color) {
        Permanent triome = harness.addToBattlefieldAndReturn(player1, new RaugrinTriome());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        ManaColor manaColor = ManaColor.valueOf(color);
        harness.handleListChoice(player1, color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(triome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RaugrinTriome()));
        harness.setLibrary(player1, List.of(new RaugrinTriome()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raugrin Triome");
        harness.assertInHand(player1, "Raugrin Triome");
    }

    @Test
    @DisplayName("Cannot produce mana while tapped on entry")
    void cannotProduceManaWhileTappedOnEntry() {
        harness.setHand(player1, List.of(new RaugrinTriome()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Produces mana after untapping but cannot tap twice")
    void producesManaAfterUntappingButCannotTapTwice() {
        harness.setHand(player1, List.of(new RaugrinTriome()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling with insufficient mana leaves the card in hand")
    void cyclingWithInsufficientManaLeavesCardInHand() {
        RaugrinTriome triome = new RaugrinTriome();
        harness.setHand(player1, List.of(triome));
        harness.setLibrary(player1, List.of(new RaugrinTriome()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(triome);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"COLORLESS", "BLUE", "RED", "WHITE", "BLACK", "GREEN"})
    @DisplayName("Cycling accepts any mana and discards before the draw resolves")
    void cyclingPaysGenericManaAndDiscardsBeforeDrawing(String color) {
        RaugrinTriome triome = new RaugrinTriome();
        RaugrinTriome drawnCard = new RaugrinTriome();
        harness.setHand(player1, List.of(triome));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.valueOf(color), 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(triome);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(triome);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
