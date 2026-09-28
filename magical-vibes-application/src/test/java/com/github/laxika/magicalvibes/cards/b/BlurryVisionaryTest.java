package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlurryVisionary.class, GrizzlyBears.class, LlanowarElves.class})
class BlurryVisionaryTest extends BaseCardTest {

    @Test
    void combinesTheTopTwoCardsWithTheChosenCardInFront() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new BlurryVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        Card combined = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(second.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(combined.isModalDoubleFaced()).isTrue();
        assertThat(combined.getBackFaceCard().getId()).isEqualTo(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsTheOnlyTopCardIntoHandWithoutMakingAnMdfc() {
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new BlurryVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(onlyCard.isModalDoubleFaced()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
