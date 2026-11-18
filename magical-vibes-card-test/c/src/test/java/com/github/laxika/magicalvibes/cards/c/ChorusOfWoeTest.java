package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChorusOfWoe.class, GrizzlyBears.class, Swamp.class})
class ChorusOfWoeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+0")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(1);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(3);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
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
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Does not boost creatures that enter after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        Permanent laterBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(PermanentGrants.powerBoostOf(gd, laterBear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterBear)).isZero();
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Includes creatures that enter while the spell is on the stack")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves without creatures or targets")
    void resolvesWithNoCreatures() {
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Chorus of Woe");
    }

    @Test
    @DisplayName("Does not boost noncreature permanents")
    void doesNotBoostNoncreatures() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, swamp)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, swamp)).isZero();
    }

    @Test
    @DisplayName("Multiple casts stack their boosts until cleanup")
    void multipleCastsStackUntilCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new ChorusOfWoe(), "{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
