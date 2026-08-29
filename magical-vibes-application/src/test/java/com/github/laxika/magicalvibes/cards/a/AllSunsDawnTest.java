package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.e.EngineeredExplosives;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.n.NayaCharm;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllSunsDawnTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    void returnsUpToOneCardOfEachColorAndExilesItself() {
        List<Card> cards = List.of(
                new HolyDay(), new CounselOfTheSoratami(), new NightsWhisper(), new Shock(), new GiantGrowth());
        harness.setGraveyard(player1, cards);
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castSorcery(player1, 0, cards.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrderElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream().map(card -> card.getName()))
                .contains("All Suns' Dawn");
    }

    @Test
    void multicoloredCardsCanFillDifferentColorGroups() {
        Card firstNayaCharm = new NayaCharm();
        Card secondNayaCharm = new NayaCharm();
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(firstNayaCharm, secondNayaCharm, counsel));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castSorcery(player1, 0, List.of(counsel.getId(), firstNayaCharm.getId(), secondNayaCharm.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrder(counsel.getId(), firstNayaCharm.getId(), secondNayaCharm.getId());
    }

    @Test
    void rejectsTwoCardsThatCanOnlyBeAssignedToTheSameColor() {
        Card firstWhiteCard = new HolyDay();
        Card secondWhiteCard = new HolyDay();
        harness.setGraveyard(player1, List.of(firstWhiteCard, secondWhiteCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstWhiteCard.getId(), secondWhiteCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one card for each color");
    }

    @Test
    void colorlessCardsAreNotLegalTargets() {
        Card colorlessCard = new EngineeredExplosives();
        harness.setGraveyard(player1, List.of(colorlessCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(colorlessCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
