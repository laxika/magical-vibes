package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlluminateHistory.class, Forest.class, GrizzlyBears.class})
class IlluminateHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Discards and draws the chosen number, then creates a Spirit at seven cards in its graveyard")
    void discardsDrawsAndCreatesSpiritAtGraveyardThreshold() {
        Card discard = new GrizzlyBears();
        Card kept = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, filler(6));
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new IlluminateHistory(), discard, kept));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(spirits).singleElement().satisfies(spirit -> {
            assertThat(spirit.getEffectivePower()).isEqualTo(3);
            assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(spirit.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
    }

    @Test
    @DisplayName("Does not create a Spirit when the controller has fewer than seven graveyard cards")
    void doesNotCreateSpiritBelowGraveyardThreshold() {
        harness.setGraveyard(player1, filler(6));
        harness.setGraveyard(player2, filler(7));
        harness.setHand(player1, List.of(new IlluminateHistory()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"));
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
