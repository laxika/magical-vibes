package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalliationAccord.class, GrizzlyBears.class, Shock.class})
class PalliationAccordTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature becoming tapped puts a palliation counter on Palliation Accord")
    void opponentCreatureTapAddsCounter() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(creature);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only opponent creatures trigger Palliation Accord")
    void ignoresOwnCreaturesAndOpponentNoncreatures() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(ownCreature);
        assertThat(gd.stack).isEmpty();

        Permanent opponentAccord = harness.addToBattlefieldAndReturn(player2, new PalliationAccord());
        tap(opponentAccord);
        assertThat(gd.stack).isEmpty();
        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isZero();
    }

    @Test
    @DisplayName("Removing a palliation counter prevents the next damage to its controller")
    void removesCounterAndPreventsNextDamage() {
        Permanent accord = harness.addToBattlefieldAndReturn(player1, new PalliationAccord());
        accord.setCounterCount(CounterType.PALLIATION, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(accord.getCounterCount(CounterType.PALLIATION)).isZero();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without a palliation counter")
    void cannotActivateWithoutCounter() {
        harness.addToBattlefield(player1, new PalliationAccord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
