package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TashasHideousLaughter.class, Forest.class, GrizzlyBears.class})
class TashasHideousLaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent exiles from their library until total mana value 20")
    void exilesUntilTotalManaValueTwenty() {
        Card controllerLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerLibraryCard));

        List<Card> opponentLibrary = new ArrayList<>();
        opponentLibrary.add(new Forest());
        for (int i = 0; i < 10; i++) {
            opponentLibrary.add(new GrizzlyBears());
        }
        Card remainingCard = new Forest();
        opponentLibrary.add(remainingCard);
        harness.setLibrary(player2, opponentLibrary);

        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(opponentLibrary.subList(0, 11).stream()
                        .map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.exilePlayWithoutPayingManaCost).isEmpty();
    }

    @Test
    @DisplayName("Exiles the whole library when it cannot reach total mana value 20")
    void exilesWholeLibraryWhenThresholdCannotBeReached() {
        Card land = new Forest();
        Card spell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, spell));
        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land, spell);
    }
}
