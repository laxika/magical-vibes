package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonkIdealistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted enchantment card from the graveyard to hand")
    void returnsEnchantmentFromGraveyardToHand() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        castMonkIdealist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("ETB only allows enchantment cards to be targeted")
    void onlyEnchantmentCardsAreValidTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(bears, pacifism));

        castMonkIdealist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(pacifism.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB fizzles if the targeted enchantment leaves the graveyard")
    void fizzlesIfTargetLeavesGraveyard() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        castMonkIdealist();
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Pacifism");
    }

    @Test
    @DisplayName("ETB does not prompt when the graveyard has no enchantment cards")
    void noValidEnchantmentDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castMonkIdealist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castMonkIdealist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MonkIdealist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
