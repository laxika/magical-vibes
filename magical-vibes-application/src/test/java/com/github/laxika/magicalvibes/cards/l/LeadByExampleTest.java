package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeadByExample.class, CanopyGorger.class, Wastes.class})
class LeadByExampleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two target creatures")
    void putsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CanopyGorger());

        castLeadByExample(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May target only one creature")
    void putsCounterOnOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());

        castLeadByExample(List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May choose no creatures")
    void mayChooseNoCreatures() {
        castLeadByExample(List.of());

        harness.assertInGraveyard(player1, "Lead by Example");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new Wastes());
        harness.setHand(player1, List.of(new LeadByExample()));
        addManaForLeadByExample();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wastes.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        harness.setHand(player1, List.of(new LeadByExample()));
        addManaForLeadByExample();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void cannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new CanopyGorger());
        harness.setHand(player1, List.of(new LeadByExample()));
        addManaForLeadByExample();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 2 targets");
    }

    @Test
    @DisplayName("Still puts a counter on the remaining target when one leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CanopyGorger());
        harness.setHand(player1, List.of(new LeadByExample()));
        addManaForLeadByExample();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Lead by Example");
    }

    private void castLeadByExample(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new LeadByExample()));
        addManaForLeadByExample();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addManaForLeadByExample() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
