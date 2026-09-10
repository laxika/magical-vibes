package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SickAndTired.class, GiantCockroach.class, ThranLens.class})
class SickAndTiredTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gives both target creatures -1/-1 until end of turn")
    void weakensBothTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new SickAndTired()));
        giveMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SickAndTired()));
        giveMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresExactlyTwoCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SickAndTired()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranLens());
        harness.setHand(player1, List.of(new SickAndTired()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDuplicateCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SickAndTired()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
