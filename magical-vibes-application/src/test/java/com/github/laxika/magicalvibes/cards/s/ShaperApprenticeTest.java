package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShaperApprenticeTest extends BaseCardTest {

    // ===== Conditional flying with another Merfolk =====

    @Test
    @DisplayName("Has flying when controller controls another Merfolk")
    void hasFlyingWithAnotherMerfolk() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, createMerfolk());

        Permanent apprentice = findPermanent(player1, "Shaper Apprentice");
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("No flying without another Merfolk")
    void noFlyingWithoutAnotherMerfolk() {
        harness.addToBattlefield(player1, new ShaperApprentice());

        Permanent apprentice = findPermanent(player1, "Shaper Apprentice");
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with a non-Merfolk creature")
    void noFlyingWithNonMerfolkCreature() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent apprentice = findPermanent(player1, "Shaper Apprentice");
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    // ===== Two Shaper Apprentices grant each other flying =====

    @Test
    @DisplayName("Two Shaper Apprentices each have flying (they are each other's 'another Merfolk')")
    void twoApprenticesGrantEachOtherFlying() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, new ShaperApprentice());

        List<Permanent> apprentices = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shaper Apprentice"))
                .toList();

        assertThat(apprentices).hasSize(2);
        assertThat(gqs.hasKeyword(gd, apprentices.get(0), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, apprentices.get(1), Keyword.FLYING)).isTrue();
    }

    // ===== Loses flying when Merfolk leaves =====

    @Test
    @DisplayName("Loses flying when the other Merfolk leaves the battlefield")
    void losesFlyingWhenMerfolkLeaves() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, createMerfolk());

        Permanent apprentice = findPermanent(player1, "Shaper Apprentice");
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();

        // Remove the other Merfolk
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getCard().getName().equals("Shaper Apprentice")
                        && p.getCard().getSubtypes().contains(CardSubtype.MERFOLK));

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    // ===== Opponent's Merfolk doesn't count =====

    @Test
    @DisplayName("Opponent's Merfolk does not grant flying")
    void opponentMerfolkDoesNotCount() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player2, createMerfolk());

        Permanent apprentice = findPermanent(player1, "Shaper Apprentice");
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    // ===== Helper methods =====

    private Card createMerfolk() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.MERFOLK));
        return card;
    }

}
