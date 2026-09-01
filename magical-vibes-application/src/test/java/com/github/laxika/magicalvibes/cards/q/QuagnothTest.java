package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Quagnoth.class, Distress.class, Sift.class, GrizzlyBears.class})
class QuagnothTest extends BaseCardTest {

    @Test
    void returnsToHandWhenDiscardedByOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(new Quagnoth())));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Quagnoth");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Quagnoth");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Quagnoth");
    }

    @Test
    void doesNotTriggerWhenControllerDiscardsIt() {
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new Sift(), new Quagnoth()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quagnoth");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Quagnoth");
    }
}
