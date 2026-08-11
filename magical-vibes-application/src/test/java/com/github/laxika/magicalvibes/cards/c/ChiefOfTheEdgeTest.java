package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChiefOfTheEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Other Warriors you control get +1/+0")
    void boostsOtherWarriorsYouControl() {
        harness.addToBattlefield(player1, new ChiefOfTheEdge());
        harness.addToBattlefield(player1, new ChiefOfTheEdge());

        assertThat(findPermanents(player1, "Chief of the Edge").stream()
                .mapToInt(permanent -> gqs.getEffectivePower(gd, permanent)))
                .containsExactly(4, 4);
        assertThat(findPermanents(player1, "Chief of the Edge").stream()
                .mapToInt(permanent -> gqs.getEffectiveToughness(gd, permanent)))
                .containsExactly(2, 2);
    }

    @Test
    @DisplayName("Chief of the Edge does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new ChiefOfTheEdge());

        Permanent chief = findPermanent(player1, "Chief of the Edge");
        assertThat(gqs.getEffectivePower(gd, chief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chief)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Warrior creatures and opponents' Warriors are unaffected")
    void onlyBoostsOtherOwnWarriors() {
        harness.addToBattlefield(player1, new ChiefOfTheEdge());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ChiefOfTheEdge());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentChief = findPermanent(player2, "Chief of the Edge");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentChief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentChief)).isEqualTo(2);
    }
}
