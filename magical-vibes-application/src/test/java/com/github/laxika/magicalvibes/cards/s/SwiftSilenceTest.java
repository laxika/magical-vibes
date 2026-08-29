package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftSilence.class, GrizzlyBears.class, Forest.class})
class SwiftSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters all other spells and draws for each spell countered")
    void countersAllOtherSpellsAndDrawsForEach() {
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SwiftSilence()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        int player2HandBeforeCast = gd.playerHands.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBeforeCast + 1);
        harness.assertInGraveyard(player2, "Swift Silence");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
