package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostfistStrider.class, GrizzlyBears.class, Shock.class})
class FrostfistStriderTest extends BaseCardTest {

    @Test
    void entersByTappingAnOpponentsCreatureAndGivingItAStunCounter() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FrostfistStrider()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void stunCounterIsConsumedByTheNextUntapStep() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FrostfistStrider()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void wardCountersAnOpponentSpellUnlessTheyPayTwoMana() {
        Permanent frostfist = harness.addToBattlefieldAndReturn(player1, new FrostfistStrider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, frostfist.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(frostfist.getMarkedDamage()).isZero();
    }

    @Test
    void payingTwoManaLetsAnOpponentsSpellResolve() {
        Permanent frostfist = harness.addToBattlefieldAndReturn(player1, new FrostfistStrider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, frostfist.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(frostfist.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetYourOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FrostfistStrider()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
