package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmberWeaverTest extends BaseCardTest {

    // ===== +1/+0 and first strike while controlling a red permanent =====

    @Test
    @DisplayName("Gets +1/+0 and first strike while controller controls a red permanent")
    void boostedWithRedPermanent() {
        harness.addToBattlefield(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new HillGiant()); // red permanent

        Permanent weaver = findPermanent(player1, "Ember Weaver");
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3); // 2 base + 1
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== No red permanent =====

    @Test
    @DisplayName("No boost or first strike without a red permanent")
    void noBoostWithoutRedPermanent() {
        harness.addToBattlefield(player1, new EmberWeaver());

        Permanent weaver = findPermanent(player1, "Ember Weaver");
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2); // base
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-red permanent does not grant the bonus")
    void nonRedPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green

        Permanent weaver = findPermanent(player1, "Ember Weaver");
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Loses bonus when the red permanent leaves =====

    @Test
    @DisplayName("Loses +1/+0 and first strike when the red permanent leaves the battlefield")
    void losesBonusWhenRedPermanentLeaves() {
        harness.addToBattlefield(player1, new EmberWeaver());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent weaver = findPermanent(player1, "Ember Weaver");
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getColors().contains(CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Opponent's red permanent doesn't count =====

    @Test
    @DisplayName("An opponent's red permanent does not grant the bonus")
    void opponentRedPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new EmberWeaver());
        harness.addToBattlefield(player2, new HillGiant());

        Permanent weaver = findPermanent(player1, "Ember Weaver");
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.FIRST_STRIKE)).isFalse();
    }

}
