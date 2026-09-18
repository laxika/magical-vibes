package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InscribedTablet.class, Forest.class, GrizzlyBears.class})
class InscribedTabletTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts a chosen land from the top five into hand")
    void findsLandAmongTopFive() {
        Permanent tablet = addTablet();
        Card keptLand = new Forest();
        Card otherLand = new Forest();
        Card nonland1 = new GrizzlyBears();
        Card nonland2 = new GrizzlyBears();
        Card nonland3 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(keptLand, nonland1, otherLand, nonland2, nonland3));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(tablet), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .validCardIds()).containsExactly(keptLand.getId(), otherLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(keptLand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(keptLand);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(otherLand, nonland1, nonland2, nonland3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tablet);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tablet.getCard());
    }

    @Test
    @DisplayName("Draws when the top five contain no land")
    void drawsWhenNoLandIsFound() {
        Permanent tablet = addTablet();
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        Card top3 = new GrizzlyBears();
        Card top4 = new GrizzlyBears();
        Card top5 = new GrizzlyBears();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4, top5, draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(tablet), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(top1, top2, top3, top4, top5);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tablet);
    }

    private Permanent addTablet() {
        return harness.addToBattlefieldAndReturn(player1, new InscribedTablet());
    }

    private int battlefieldIndex(Permanent tablet) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(tablet);
    }
}
