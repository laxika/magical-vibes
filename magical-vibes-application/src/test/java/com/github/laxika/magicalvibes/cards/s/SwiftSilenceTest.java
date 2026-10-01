package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IgnorantBliss;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftSilence.class, IgnorantBliss.class})
class SwiftSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters all other spells and draws for each spell countered")
    void countersAllOtherSpellsAndDrawsForEach() {
        harness.setLibrary(player2,
                List.of(new IgnorantBliss(), new IgnorantBliss(), new IgnorantBliss()));

        harness.castFromHand(player1, new IgnorantBliss(), "{1}{R}");
        harness.castFromHand(player1, new IgnorantBliss(), "{1}{R}");
        harness.passPriority(player1);
        harness.castFromHand(player2, new SwiftSilence(), "{2}{W}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ignorant Bliss", "Ignorant Bliss");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Swift Silence");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not count itself as a spell to counter")
    void doesNotCountItselfAsOtherSpell() {
        harness.setLibrary(player1, List.of(new IgnorantBliss()));
        harness.castFromHand(player1, new SwiftSilence(), "{2}{W}{U}{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Swift Silence");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters another spell controlled by its own controller")
    void countersOwnOtherSpell() {
        harness.setLibrary(player1, List.of(new IgnorantBliss()));
        harness.castFromHand(player1, new IgnorantBliss(), "{1}{R}");
        harness.castFromHand(player1, new SwiftSilence(), "{2}{W}{U}{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ignorant Bliss");
        harness.assertInGraveyard(player1, "Swift Silence");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
