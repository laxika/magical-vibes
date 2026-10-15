package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bloodbriar.class, GrizzlyBears.class, DiabolicEdict.class})
class BloodbriarTest extends BaseCardTest {

    @Test
    void growsWhenAnotherPermanentYouControlIsSacrificed() {
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveEdictAt(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrowWhenOpponentSacrificesAPermanent() {
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolveEdictAt(player2);
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenBloodbriarItselfIsSacrificed() {
        harness.addToBattlefield(player1, new Bloodbriar());

        castAndResolveEdictAt(player1);

        harness.assertInGraveyard(player1, "Bloodbriar");
        harness.assertNotOnBattlefield(player1, "Bloodbriar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(Terrarion.class)
    void growsForEachNoncreaturePermanentSacrificedAsAnActivationCost() {
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        harness.addToBattlefield(player1, new Terrarion());
        harness.addToBattlefield(player1, new Terrarion());

        for (int expectedCounters = 1; expectedCounters <= 2; expectedCounters++) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 1, null, null);
            harness.handleListChoice(player1, "GREEN");
            harness.handleListChoice(player1, "GREEN");
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    .isEqualTo(expectedCounters);
        }
    }

    private void castAndResolveEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
