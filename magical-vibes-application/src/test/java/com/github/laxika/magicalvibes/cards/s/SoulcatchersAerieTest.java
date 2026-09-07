package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulcatchersAerie.class, SuntailHawk.class, GrizzlyBears.class, Shock.class})
class SoulcatchersAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a feather counter on itself when a Bird is put into its controller's graveyard")
    void putsFeatherCounterWhenBirdDies() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        killWithShock(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Bird creature")
    void doesNotTriggerForNonBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, bears);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a Bird put into an opponent's graveyard")
    void doesNotTriggerForOpponentsBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        killWithShock(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Bird creatures get +1/+1 for each feather counter")
    void boostsBirdsByFeatherCounterCount() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        aerie.setCounterCount(CounterType.FEATHER, 2);

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(3);
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
