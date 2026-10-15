package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishLancer.class, Repulse.class})
class BenalishLancerTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrFirstStrike() {
        harness.castFromHand(player1, new BenalishLancer(), "{2}{W}");
        harness.passBothPriorities();

        Permanent lancer = findLancer();
        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndFirstStrike() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lancer = findLancer();
        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void kickedFirstStrikeDoesNotWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent lancer = findLancer();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void enteringWithoutBeingCastDoesNotGrantKickerBenefits() {
        Permanent lancer = harness.enterBattlefieldAndReturn(player1, new BenalishLancer());

        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returningKickedLancerToHandAndRecastingWithoutKickerLosesBenefits() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent kickedLancer = findLancer();
        assertThat(kickedLancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kickedLancer, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new BenalishLancer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, kickedLancer.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Benalish Lancer");
        harness.assertInHand(player1, "Benalish Lancer");

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recastLancer = findLancer();
        assertThat(recastLancer.getId()).isNotEqualTo(kickedLancer.getId());
        assertThat(recastLancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, recastLancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent findLancer() {
        return findPermanent(player1, "Benalish Lancer");
    }
}
