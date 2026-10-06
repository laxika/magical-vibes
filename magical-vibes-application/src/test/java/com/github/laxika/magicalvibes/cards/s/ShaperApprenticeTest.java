package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.r.RangingRaptors;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaperApprentice.class, JungleDelver.class, RangingRaptors.class})
class ShaperApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying when controller controls another Merfolk")
    void hasFlyingWithAnotherMerfolk() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, new JungleDelver());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("No flying without another Merfolk")
    void noFlyingWithoutAnotherMerfolk() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with a non-Merfolk creature")
    void noFlyingWithNonMerfolkCreature() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, new RangingRaptors());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Two Shaper Apprentices each have flying (they are each other's 'another Merfolk')")
    void twoApprenticesGrantEachOtherFlying() {
        harness.addToBattlefield(player1, new ShaperApprentice());
        harness.addToBattlefield(player1, new ShaperApprentice());

        List<Permanent> apprentices = findPermanents(player1, "Shaper Apprentice");

        assertThat(apprentices).hasSize(2);
        assertThat(gqs.hasKeyword(gd, apprentices.get(0), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, apprentices.get(1), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the other Merfolk leaves the battlefield")
    void losesFlyingWhenMerfolkLeaves() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new JungleDelver());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(merfolk);

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Merfolk does not grant flying")
    void opponentMerfolkDoesNotCount() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());
        harness.addToBattlefield(player2, new JungleDelver());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying immediately when another Merfolk enters, without granting it to that Merfolk")
    void gainsFlyingWhenMerfolkEnters() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new ShaperApprentice());
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();

        Permanent merfolk = harness.enterBattlefieldAndReturn(player1, new JungleDelver());

        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isFalse();
    }

}
