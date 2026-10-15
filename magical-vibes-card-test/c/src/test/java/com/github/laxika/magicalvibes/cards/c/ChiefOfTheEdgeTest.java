package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarduHateblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChiefOfTheEdge.class, GrizzlyBears.class, MarduHateblade.class})
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
        Permanent chief = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        assertThat(gqs.getEffectivePower(gd, chief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chief)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Warrior creatures and opponents' Warriors are unaffected")
    void onlyBoostsOtherOwnWarriors() {
        harness.addToBattlefield(player1, new ChiefOfTheEdge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentChief = harness.addToBattlefieldAndReturn(player2, new ChiefOfTheEdge());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentChief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentChief)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Warrior entering later gains the bonus only while the Chief remains")
    void bonusUpdatesWhenWarriorEntersAndChiefLeaves() {
        Permanent chief = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chief);

        harness.assertInGraveyard(player1, "Chief of the Edge");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(1);
    }
}
