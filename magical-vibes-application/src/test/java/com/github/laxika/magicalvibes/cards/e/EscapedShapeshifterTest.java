package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscapedShapeshifterTest extends BaseCardTest {

    private Permanent shapeshifter() {
        return findPermanent(player1, "Escaped Shapeshifter");
    }

    @Test
    @DisplayName("Gains flying while an opponent controls a creature with flying")
    void gainsFlyingFromOpponentFlyer() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gains first strike and trample from the matching opponent creatures")
    void gainsFirstStrikeAndTrample() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new YouthfulKnight()); // First strike, flying-less
        harness.addToBattlefield(player2, new AvatarOfMight()); // Trample

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each ability is checked independently — a vanilla opponent creature grants nothing")
    void grantsNothingWithoutMatchingAbility() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only opponents' creatures count, not the controller's own")
    void ownFlyerDoesNotGrantFlying() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player1, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the granted ability as soon as the opponent's flyer leaves")
    void losesFlyingWhenOpponentFlyerLeaves() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Suntail Hawk"));

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Protection is granted per color: an opponent's White Knight grants protection from black only")
    void gainsProtectionFromBlackOnly() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new WhiteKnight()); // Protection from black

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Two opponent creatures with different protections grant both colors")
    void gainsProtectionFromTwoColors() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new WhiteKnight()); // Protection from black
        harness.addToBattlefield(player2, new BlackKnight()); // Protection from white

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Alone on the battlefield it has none of the four abilities")
    void grantsNothingAlone() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's own Escaped Shapeshifter is excluded by name")
    void opponentCopyIsExcludedByName() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SuntailHawk());

        // player1's copy sees the opponent's Hawk and gains flying. player2's copy only sees
        // player1's Shapeshifter, which the name exclusion rejects even though it now flies.
        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Escaped Shapeshifter"), Keyword.FLYING)).isFalse();
    }
}
