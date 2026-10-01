package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuggleThePerformance.class, Forest.class, GrizzlyBears.class})
class JuggleThePerformanceTest extends BaseCardTest {

    @Test
    void discardsEachHandAndConjuresSevenDuplicatesFromThePlayerToTheRight() {
        harness.setHand(player1, List.of(new JuggleThePerformance(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7)
                .allMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7)
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(card -> gd.perpetualAnyColorManaForCastCardIds.contains(card.getId())
                        && !card.getId().equals(gd.playerDecks.get(player2.getId()).getFirst().getId())
                        && card.getOwnerId().equals(player1.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .allMatch(card -> gd.perpetualAnyColorManaForCastCardIds.contains(card.getId())
                        && !card.getId().equals(gd.playerDecks.get(player1.getId()).getFirst().getId())
                        && card.getOwnerId().equals(player2.getId()));
    }
}
