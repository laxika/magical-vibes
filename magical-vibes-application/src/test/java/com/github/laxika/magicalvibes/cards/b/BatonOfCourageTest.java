package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.g.GuardianIdol;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BatonOfCourage.class, GoblinBrawler.class, GuardianIdol.class})
class BatonOfCourageTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new BatonOfCourage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent baton = findPermanent(player1, "Baton of Courage");
        assertThat(baton.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new BatonOfCourage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent baton = findPermanent(player1, "Baton of Courage");
        assertThat(baton.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void sunburstIgnoresColorlessMana() {
        harness.setHand(player1, List.of(new BatonOfCourage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent baton = findPermanent(player1, "Baton of Courage");
        assertThat(baton.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BatonOfCourage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void removesChargeCounterAndBoostsTargetCreatureUntilEndOfTurn() {
        Permanent baton = addReadyBaton(player1);
        baton.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = addCreatureReady(player2, new GoblinBrawler());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(baton.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutAChargeCounter() {
        addReadyBaton(player1);
        Permanent creature = addCreatureReady(player2, new GoblinBrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent baton = addReadyBaton(player1);
        baton.setCounterCount(CounterType.CHARGE, 1);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new GuardianIdol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBaton(Player player) {
        return addCreatureReady(player, new BatonOfCourage());
    }
}
