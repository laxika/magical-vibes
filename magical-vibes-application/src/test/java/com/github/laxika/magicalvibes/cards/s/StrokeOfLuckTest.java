package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrokeOfLuck.class, GrizzlyBears.class, Shock.class})
class StrokeOfLuckTest extends BaseCardTest {

    @Test
    void choosesANameAndPutsAllMatchingLookedAtCardsIntoHand() {
        Card firstBears = new GrizzlyBears();
        Card shock = new Shock();
        Card secondBears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(firstBears, shock, secondBears, secondShock, untouched));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstBears, shock, secondBears, secondShock);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBears, secondBears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(shock, secondShock, untouched);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void losesOneLifeWhenTheChosenNameAppearsOnce() {
        Card bears = new GrizzlyBears();
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        Card thirdShock = new Shock();
        harness.setLibrary(player1, List.of(bears, firstShock, secondShock, thirdShock));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                firstShock, secondShock, thirdShock);
    }
}
