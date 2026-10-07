package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FathomFleetCutthroat;
import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrashOfRaptors.class, NestRobber.class, FathomFleetCutthroat.class})
class ThrashOfRaptorsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 (becomes 5/3) when controller controls another Dinosaur")
    void boostWithAnotherDinosaur() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    @Test
    @DisplayName("Base 3/3 without another Dinosaur")
    void noBoostWithoutAnotherDinosaur() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost with a non-Dinosaur creature")
    void noBoostWithNonDinosaur() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new FathomFleetCutthroat());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    @Test
    @DisplayName("Has trample when controller controls another Dinosaur")
    void hasTrampleWithAnotherDinosaur() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new NestRobber());

        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not have trample when alone (no other Dinosaur)")
    void noTrampleWithoutAnotherDinosaur() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());

        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses +2/+0 and trample when the other Dinosaur leaves the battlefield")
    void losesBoostAndTrampleWhenDinosaurLeaves() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, findPermanent(player1, "Nest Robber")));

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Dinosaur does not grant boost or trample")
    void opponentDinosaurDoesNotCount() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player2, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Two Thrash of Raptors grant each other +2/+0 and trample")
    void twoThrashOfRaptorsEnableEachOther() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new ThrashOfRaptors());

        List<Permanent> thrashes = findPermanents(player1, "Thrash of Raptors");

        assertThat(thrashes).hasSize(2);
        for (Permanent thrash : thrashes) {
            assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Gains the bonus immediately when another Dinosaur enters")
    void gainsBonusWhenAnotherDinosaurEnters() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bonus does not stack with multiple other Dinosaurs")
    void multipleDinosaursGrantOnlyOneBonus() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new NestRobber());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, other));

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent thrash = harness.addToBattlefieldAndReturn(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new NestRobber());

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);

        thrash.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

}
