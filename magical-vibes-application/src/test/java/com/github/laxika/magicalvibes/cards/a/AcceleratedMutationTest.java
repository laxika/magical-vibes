package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcceleratedMutation.class, ElvishAberration.class, Stabilizer.class, TreetopScout.class})
class AcceleratedMutationTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusGreatestManaValueAmongYourPermanents() {
        harness.addToBattlefield(player1, new ElvishAberration());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(6);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    void evaluatesGreatestManaValueAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TreetopScout());
        Permanent elvishAberration = harness.addToBattlefieldAndReturn(player1, new ElvishAberration());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elvishAberration);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void countsNonCreaturePermanentsYouControl() {
        harness.addToBattlefield(player1, new Stabilizer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void ignoresHigherManaValuePermanentsControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        harness.addToBattlefield(player2, new ElvishAberration());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void boostWearsOffAtCleanupStep() {
        harness.addToBattlefield(player1, new ElvishAberration());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Stabilizer());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void givesNoBoostWhenTheControllerHasNoPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreetopScout());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
