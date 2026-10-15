package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GriffinRider.class, WildGriffin.class, AlabornTrooper.class,
        ArtificialEvolution.class, Bitterblossom.class})
class GriffinRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 with no flying when no Griffin is controlled")
    void noBoostWithoutGriffin() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No boost with a non-Griffin creature")
    void noBoostWithNonGriffin() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        harness.addToBattlefield(player1, new AlabornTrooper());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +3/+3 and flying while controlling a Griffin")
    void boostWithGriffin() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        harness.addToBattlefield(player1, new WildGriffin());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Griffin does not grant the bonus")
    void opponentGriffinDoesNotCount() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        harness.addToBattlefield(player2, new WildGriffin());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Griffin leaves the battlefield")
    void losesBonusWhenGriffinLeaves() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        harness.addToBattlefield(player1, new WildGriffin());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Wild Griffin"));

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple Griffins grant the bonus only once")
    void multipleGriffinsDoNotStackBonus() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        harness.addToBattlefield(player1, new WildGriffin());
        harness.addToBattlefield(player1, new WildGriffin());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A noncreature Griffin permanent does not grant the bonus")
    void noncreatureGriffinDoesNotCount() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new GriffinRider());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, blossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "GRIFFIN");

        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.GRIFFIN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.FLYING)).isFalse();
    }
}
