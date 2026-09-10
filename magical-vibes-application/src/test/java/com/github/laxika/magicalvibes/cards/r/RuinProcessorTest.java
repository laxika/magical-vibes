package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinProcessor.class, PathToExile.class})
class RuinProcessorTest extends BaseCardTest {

    @Test
    void putsAnOpponentOwnedExiledCardIntoItsOwnersGraveyardAndGainsLife() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void mayDeclineAndDoesNotGainLife() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        castRuinProcessor();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    @Test
    void doesNotPromptForCardsOwnedByTheController() {
        harness.setExile(player1, List.of(new PathToExile()));
        castRuinProcessor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ruin Processor");
    }

    private void castRuinProcessor() {
        harness.setHand(player1, List.of(new RuinProcessor()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
