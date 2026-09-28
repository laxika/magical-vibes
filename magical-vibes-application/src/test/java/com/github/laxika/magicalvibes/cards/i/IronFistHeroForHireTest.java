package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronFistHeroForHire.class, GrizzlyBears.class, Mountain.class, Shock.class})
class IronFistHeroForHireTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up is discounted on the entry turn and deals damage before adding counters")
    void powerUpIsDiscountedOnEntryTurn() {
        Permanent ironFist = harness.enterBattlefieldAndReturn(player1, new IronFistHeroForHire());
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 5);
        assertThat(ironFist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power-up divides five damage among up to five targets")
    void powerUpDividesDamageAmongFiveTargets() {
        Permanent ironFist = addCreatureReady(player1, new IronFistHeroForHire());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        Permanent fourth = addCreatureReady(player2, new GrizzlyBears());
        Permanent fifth = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1,
                fourth.getId(), 1,
                fifth.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(1);
        assertThat(fourth.getMarkedDamage()).isEqualTo(1);
        assertThat(fifth.getMarkedDamage()).isEqualTo(1);
        assertThat(ironFist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prowess triggers for a noncreature spell and expires at end of turn")
    void prowessWearsOffAtEndOfTurn() {
        Permanent ironFist = addCreatureReady(player1, new IronFistHeroForHire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ironFist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ironFist)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ironFist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ironFist)).isEqualTo(2);
    }

    @Test
    void powerUpCannotTargetALand() {
        addCreatureReady(player1, new IronFistHeroForHire());
        Permanent mountain = addCreatureReady(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(mountain.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }
}
