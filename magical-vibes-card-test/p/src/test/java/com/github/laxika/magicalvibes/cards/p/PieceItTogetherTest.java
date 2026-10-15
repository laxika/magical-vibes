package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PieceItTogether.class, Forest.class})
class PieceItTogetherTest extends BaseCardTest {

    @Test
    void drawsAndIntensifiesOwnedCopiesBeforeReachingFour() {
        PieceItTogether spell = new PieceItTogether();
        PieceItTogether otherCopy = new PieceItTogether();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell, otherCopy));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherCopy, drawn);
        assertThat(gd.getCardIntensity(spell.getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(otherCopy.getId())).isEqualTo(1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void takesAnExtraTurnAtFourAndDrawsAgainAboveFour() {
        List<Card> spells = List.of(
                new PieceItTogether(), new PieceItTogether(), new PieceItTogether(),
                new PieceItTogether(), new PieceItTogether(), new PieceItTogether());
        harness.setHand(player1, spells);
        harness.addMana(player1, ManaColor.BLUE, 6);

        for (int i = 0; i < 5; i++) {
            harness.castSorcery(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getCardIntensity(spells.get(4).getId())).isEqualTo(5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getCardIntensity(spells.get(5).getId())).isEqualTo(6);
    }
}
