package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Solidarity.class, GrizzlyBears.class, Plains.class})
class SolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +0/+5")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(5);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(7);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Only creatures present at resolution receive the boost")
    void onlyCreaturesPresentAtResolutionAreBoosted() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(5);
        assertThat(PermanentGrants.powerBoostOf(gd, plains)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, plains)).isZero();

        Permanent laterBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(PermanentGrants.powerBoostOf(gd, laterBear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterBear)).isZero();
    }

    @Test
    @DisplayName("Resolves when you control no creatures")
    void resolvesWithNoCreatures() {
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Solidarity");
    }

    @Test
    @DisplayName("Creatures entering while Solidarity is on the stack receive the boost")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        Permanent bear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Solidarity");
    }

    @Test
    @DisplayName("Two Solidarity spells stack their toughness boosts")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(12);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Solidarity(), "{3}{W}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(2);
            }
        }
    }
}
