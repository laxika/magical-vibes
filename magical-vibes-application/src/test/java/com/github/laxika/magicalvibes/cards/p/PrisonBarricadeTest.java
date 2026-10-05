package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CapashenUnicorn;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrisonBarricade.class, CapashenUnicorn.class, Repulse.class})
class PrisonBarricadeTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounterOrAllowAttacking() {
        harness.castFromHand(player1, new PrisonBarricade(), "{1}{W}");
        harness.passBothPriorities();

        Permanent barricade = findPermanent(player1, "Prison Barricade");
        assertThat(barricade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        barricade.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void castWithKickerEntersWithCounterAndCanAttackDespiteDefender() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent barricade = findPermanent(player1, "Prison Barricade");
        assertThat(barricade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        barricade.setSummoningSick(false);
        harness.addToBattlefield(player2, new CapashenUnicorn());

        declareAttackers(List.of(0));

        assertThat(barricade.isAttacking()).isTrue();
    }

    @Test
    void kickedAttackPermissionLastsBeyondTurnOfEntry() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent barricade = findPermanent(player1, "Prison Barricade");
        barricade.setSummoningSick(false);
        harness.addToBattlefield(player2, new CapashenUnicorn());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(barricade.isAttacking()).isTrue();
    }

    @Test
    void kickedCreatureStillCannotAttackWhileSummoningSick() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void returningToHandAndRecastingWithoutKickerLosesCounterAndAttackPermission() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent original = findPermanent(player1, "Prison Barricade");
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new CapashenUnicorn()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, original.getId());
        harness.assertNotOnBattlefield(player1, "Prison Barricade");
        harness.assertInHand(player1, "Prison Barricade");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Prison Barricade");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        returned.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
