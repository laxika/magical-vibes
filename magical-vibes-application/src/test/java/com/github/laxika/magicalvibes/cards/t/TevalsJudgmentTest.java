package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.k.KrosanReclamation;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TevalsJudgment.class, KrosanReclamation.class, Pariah.class})
class TevalsJudgmentTest extends BaseCardTest {

    private static final String DRAW = "Draw a card.";
    private static final String TREASURE = "Create a Treasure token.";
    private static final String ZOMBIE = "Create a 2/2 black Zombie Druid creature token.";

    @Test
    @DisplayName("Draw mode draws a card when cards leave the controller's graveyard")
    void drawMode() {
        addJudgmentAndReclamation();

        triggerAndChoose(DRAW);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(Pariah.class);
    }

    @Test
    @DisplayName("Treasure mode creates a Treasure token")
    void treasureMode() {
        addJudgmentAndReclamation();

        triggerAndChoose(TREASURE);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Zombie Druid mode creates a 2/2 black Zombie Druid token")
    void zombieDruidMode() {
        addJudgmentAndReclamation();

        triggerAndChoose(ZOMBIE);

        assertThat(findPermanents(player1, "Zombie Druid")).singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                });
    }

    @Test
    @DisplayName("A chosen mode is unavailable again during the same turn")
    void modeCannotBeChosenTwiceInOneTurn() {
        addJudgmentAndReclamation();
        triggerAndChoose(DRAW);

        harness.setHand(player1, List.of(new KrosanReclamation()));
        harness.setGraveyard(player1, List.of(new KrosanReclamation()));
        harness.setLibrary(player1, List.of(new Pariah()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player1.getId());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, TREASURE);
        harness.passBothPriorities();
    }

    private void addJudgmentAndReclamation() {
        harness.addToBattlefield(player1, new TevalsJudgment());
        harness.setGraveyard(player1, List.of(new KrosanReclamation(), new KrosanReclamation()));
        harness.setHand(player1, List.of(new KrosanReclamation()));
        harness.setLibrary(player1, List.of(new Pariah()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
    }

    private void triggerAndChoose(String mode) {
        harness.castInstant(player1, 0, player1.getId());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
