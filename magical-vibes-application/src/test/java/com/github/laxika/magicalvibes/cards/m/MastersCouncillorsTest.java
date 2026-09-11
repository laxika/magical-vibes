package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MastersCouncillors.class, GrizzlyBears.class})
class MastersCouncillorsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each graveyard with seven or more cards")
    void scalesWithSevenCardGraveyards() {
        Permanent councillors = harness.addToBattlefieldAndReturn(player1, new MastersCouncillors());
        harness.setGraveyard(player1, graveyardOf(7));
        harness.setGraveyard(player2, graveyardOf(6));

        assertThat(gqs.getEffectivePower(gd, councillors)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, councillors)).isEqualTo(3);

        harness.setGraveyard(player2, graveyardOf(7));
        assertThat(gqs.getEffectivePower(gd, councillors)).isEqualTo(5);

        harness.setGraveyard(player1, graveyardOf(6));
        assertThat(gqs.getEffectivePower(gd, councillors)).isEqualTo(3);
    }

    @Test
    @DisplayName("The second card drawn each turn makes a target player mill three cards")
    void secondDrawMillsTargetPlayer() {
        harness.addToBattlefield(player1, new MastersCouncillors());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not trigger on the first or third card drawn in a turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefield(player1, new MastersCouncillors());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        drawCard(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private List<Card> graveyardOf(int count) {
        return IntStream.range(0, count).mapToObj(ignored -> (Card) new GrizzlyBears()).toList();
    }
}
