package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ALittleChat.class, GrizzlyBears.class})
class ALittleChatTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top two cards into hand and the other on the bottom")
    void choosesOneCardForHandAndBottomsTheOther() {
        Card chosen = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Casualty copies the top-two-card selection")
    void casualtyCopiesSelection() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstChosen = new GrizzlyBears();
        Card firstOther = new GrizzlyBears();
        Card secondChosen = new GrizzlyBears();
        Card secondOther = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstChosen, firstOther, secondChosen, secondOther));
        harness.setHand(player1, List.of(new ALittleChat()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, null, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstChosen.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(secondChosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstChosen, secondChosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(firstOther, secondOther);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
