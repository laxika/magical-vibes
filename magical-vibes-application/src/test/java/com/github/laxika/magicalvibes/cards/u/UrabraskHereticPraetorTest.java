package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrabraskHereticPraetor.class, Forest.class, GrizzlyBears.class})
class UrabraskHereticPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, exiles the top card and lets you play it")
    void exilesTopCardAtYourUpkeep() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("At an opponent's upkeep, replaces that player's next draw")
    void replacesOpponentsNextDraw() {
        Card replacedCard = new GrizzlyBears();
        Card laterCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(replacedCard, laterCard));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(replacedCard);
        assertThat(gd.exilePlayPermissions.get(replacedCard.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(replacedCard.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(laterCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent-upkeep replacement applies only to one draw")
    void replacementAppliesOnlyOnce() {
        Card replacedCard = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.addToBattlefield(player1, new UrabraskHereticPraetor());
        harness.setLibrary(player2, List.of(replacedCard, drawnCard));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(replacedCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }
}
