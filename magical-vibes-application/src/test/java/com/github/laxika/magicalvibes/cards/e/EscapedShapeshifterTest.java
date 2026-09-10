package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HornedSliver;
import com.github.laxika.magicalvibes.cards.s.SandstoneWarrior;
import com.github.laxika.magicalvibes.cards.s.SoltariMonk;
import com.github.laxika.magicalvibes.cards.s.SoltariPriest;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        EscapedShapeshifter.class,
        HornedSliver.class,
        SandstoneWarrior.class,
        SoltariMonk.class,
        SoltariPriest.class,
        TrainedArmodon.class,
        WindDrake.class
})
class EscapedShapeshifterTest extends BaseCardTest {

    private Permanent shapeshifter() {
        return findPermanent(player1, "Escaped Shapeshifter");
    }

    @Test
    @DisplayName("Gains flying while an opponent controls a creature with flying")
    void gainsFlyingFromOpponentFlyer() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new WindDrake());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gains first strike and trample from the matching opponent creatures")
    void gainsFirstStrikeAndTrample() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SandstoneWarrior()); // First strike, no flying
        harness.addToBattlefield(player2, new HornedSliver()); // Grants trample to Slivers

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each ability is checked independently — a vanilla opponent creature grants nothing")
    void grantsNothingWithoutMatchingAbility() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new TrainedArmodon());

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shapeshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only opponents' creatures count, not the controller's own")
    void ownFlyerDoesNotGrantFlying() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player1, new WindDrake());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses the granted ability as soon as the opponent's flyer leaves")
    void losesFlyingWhenOpponentFlyerLeaves() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, windDrake));

        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Protection is granted per color: an opponent's Soltari Monk grants protection from black only")
    void gainsProtectionFromBlackOnly() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SoltariMonk()); // Protection from black

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Two opponent creatures with different protections grant both colors")
    void gainsProtectionFromTwoColors() {
        harness.addToBattlefield(player1, new EscapedShapeshifter());
        harness.addToBattlefield(player2, new SoltariMonk()); // Protection from black
        harness.addToBattlefield(player2, new SoltariPriest()); // Protection from red

        Permanent shapeshifter = shapeshifter();

        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, shapeshifter, CardColor.RED)).isTrue();
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
        harness.addToBattlefield(player2, new WindDrake());

        // player1's copy sees the opponent's Drake and gains flying. player2's copy only sees
        // player1's Shapeshifter, which the name exclusion rejects even though it now flies.
        assertThat(gqs.hasKeyword(gd, shapeshifter(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Escaped Shapeshifter"), Keyword.FLYING)).isFalse();
    }
}
