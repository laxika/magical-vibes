package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AbbeyGargoyles;
import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Prophecy.class, AysenAbbey.class, AbbeyGargoyles.class})
class ProphecyTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a land on top of the opponent's library gains the caster 1 life")
    void gainsLifeWhenTopCardIsLand() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.put(player2.getId(),
                new ArrayList<>(List.of(new AysenAbbey(), new AbbeyGargoyles(), new AbbeyGargoyles())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Revealing a nonland card gains no life")
    void noLifeWhenTopCardIsNonland() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.put(player2.getId(),
                new ArrayList<>(List.of(new AbbeyGargoyles(), new AysenAbbey(), new AysenAbbey())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The targeted opponent's library is shuffled but keeps its size")
    void shufflesTargetLibrary() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            deck.add(i % 2 == 0 ? new AysenAbbey() : new AbbeyGargoyles());
        }
        gd.playerDecks.put(player2.getId(), new ArrayList<>(deck));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<Card> after = gd.playerDecks.get(player2.getId());
        assertThat(after).hasSize(40);
        assertThat(after).containsExactlyInAnyOrderElementsOf(deck);
        assertThat(gameLogContains(player2.getUsername() + " shuffles their library.")).isTrue();
    }

    @Test
    @DisplayName("Schedules a draw for the caster at the next upkeep")
    void schedulesDrawForCaster() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The caster draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextUpkeep() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the caster themselves")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Prophecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        assertThat(gd.stack).isEmpty();
    }
}
