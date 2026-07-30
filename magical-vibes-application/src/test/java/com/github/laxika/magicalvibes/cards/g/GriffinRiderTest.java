package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GriffinRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 with no flying when no Griffin is controlled")
    void noBoostWithoutGriffin() {
        harness.addToBattlefield(player1, new GriffinRider());

        Permanent rider = findPermanent(player1, "Griffin Rider");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Griffin creature")
    void noBoostWithNonGriffin() {
        harness.addToBattlefield(player1, new GriffinRider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent rider = findPermanent(player1, "Griffin Rider");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +3/+3 and flying while controlling a Griffin")
    void boostWithGriffin() {
        harness.addToBattlefield(player1, new GriffinRider());
        harness.addToBattlefield(player1, new WildGriffin());

        Permanent rider = findPermanent(player1, "Griffin Rider");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Griffin does not grant the bonus")
    void opponentGriffinDoesNotCount() {
        harness.addToBattlefield(player1, new GriffinRider());
        harness.addToBattlefield(player2, new WildGriffin());

        Permanent rider = findPermanent(player1, "Griffin Rider");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Griffin leaves the battlefield")
    void losesBonusWhenGriffinLeaves() {
        harness.addToBattlefield(player1, new GriffinRider());
        harness.addToBattlefield(player1, new WildGriffin());

        Permanent rider = findPermanent(player1, "Griffin Rider");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Wild Griffin"));

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }
}
