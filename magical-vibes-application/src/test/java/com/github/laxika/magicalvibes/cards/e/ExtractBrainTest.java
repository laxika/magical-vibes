package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractBrain.class, Forest.class, GrizzlyBears.class})
class ExtractBrainTest extends BaseCardTest {

    @Test
    @DisplayName("The target opponent chooses the X cards and the caster may cast a selected nonland")
    void targetOpponentChoosesCardsAndCasterCastsSelectedSpell() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card otherForest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(forest, bears, otherForest)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        addExtractBrainMana(2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.TargetPlayerChoosesCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetPlayerChoosesCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 1);
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.TargetPlayerChoosesCardsFromHandChoice.class).validIndices())
                .containsExactly(0, 2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(bears);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherForest);
    }

    @Test
    @DisplayName("A selected land is not offered for casting")
    void selectedLandIsNotOffered() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(forest, bears)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        addExtractBrainMana(1);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, bears);
    }

    @Test
    @DisplayName("When X exceeds the hand size, all cards are selected")
    void xExceedsHandSize() {
        Card bears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        addExtractBrainMana(2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(bears);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ExtractBrain()));
        addExtractBrainMana(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addExtractBrainMana(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
