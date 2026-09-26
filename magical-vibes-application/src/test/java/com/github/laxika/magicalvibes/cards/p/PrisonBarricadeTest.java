package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CapashenUnicorn;
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

@CardUsed({PrisonBarricade.class, CapashenUnicorn.class})
class PrisonBarricadeTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounterOrAllowAttacking() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
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
}
