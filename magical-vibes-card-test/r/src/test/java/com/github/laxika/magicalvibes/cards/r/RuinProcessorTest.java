package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinProcessor.class, CoralhelmGuide.class})
class RuinProcessorTest extends BaseCardTest {

    @Test
    void putsAnOpponentOwnedExiledCardIntoItsOwnersGraveyardAndGainsLife() {
        CoralhelmGuide exiledCard = new CoralhelmGuide();
        harness.setExile(player2, List.of(exiledCard));
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertNotOnBattlefield(player1, "Ruin Processor");
        harness.assertInGraveyard(player2, "Coralhelm Guide");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertLife(player1, 25);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void mayDeclineAndDoesNotGainLife() {
        CoralhelmGuide exiledCard = new CoralhelmGuide();
        harness.setExile(player2, List.of(exiledCard));
        castRuinProcessor();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void doesNotPromptForCardsOwnedByTheController() {
        harness.setExile(player1, List.of(new CoralhelmGuide()));
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void doesNotGainLifeWhenExileIsEmpty() {
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void enteringWithoutBeingCastDoesNotProcessOrGainLife() {
        CoralhelmGuide exiledCard = new CoralhelmGuide();
        harness.setExile(player2, List.of(exiledCard));

        harness.enterBattlefieldAndReturn(player1, new RuinProcessor());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void processesOnlyTheChosenCardWhenSeveralAreExiled() {
        CoralhelmGuide chosenCard = new CoralhelmGuide();
        CoralhelmGuide otherCard = new CoralhelmGuide();
        harness.setExile(player2, List.of(chosenCard, otherCard));
        castRuinProcessor();

        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gd.findExiledCard(chosenCard.getId())).isNull();
        assertThat(gd.findExiledCard(otherCard.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosenCard).doesNotContain(otherCard);
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void canProcessASingleOpponentOwnedFaceDownExiledCard() {
        CoralhelmGuide exiledCard = new CoralhelmGuide();
        harness.inMutationScope(() -> gd.addToExile(player2.getId(), exiledCard, null, true));
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Coralhelm Guide");
        harness.assertLife(player1, 25);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    private void castRuinProcessor() {
        harness.castFromHand(player1, new RuinProcessor(), "{7}");
        harness.passBothPriorities();
    }
}
