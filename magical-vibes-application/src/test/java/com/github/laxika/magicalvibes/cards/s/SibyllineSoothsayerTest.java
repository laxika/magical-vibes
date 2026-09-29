package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SibyllineSoothsayer.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class})
class SibyllineSoothsayerTest extends BaseCardTest {

    @Test
    void exilesFirstNonlandWithManaValueAtLeastThreeAndSuspendsIt() {
        Forest land = new Forest();
        GrizzlyBears belowThreshold = new GrizzlyBears();
        CounselOfTheSoratami qualifyingCard = new CounselOfTheSoratami();
        GrizzlyBears afterQualifyingCard = new GrizzlyBears();
        setUpSoothsayer(List.of(land, belowThreshold, qualifyingCard, afterQualifyingCard));

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(qualifyingCard);
        assertThat(gd.exiledCardTimeCounters).containsEntry(qualifyingCard.getId(), 3);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, belowThreshold, afterQualifyingCard);
    }

    @Test
    void bottomsAllRevealedCardsWhenNoQualifyingCardExists() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        setUpSoothsayer(List.of(land, nonland));

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, nonland);
    }

    private void setUpSoothsayer(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SibyllineSoothsayer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
