package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MattMurdockJusticeSeeker.class, GrizzlyBears.class, Shock.class})
class MattMurdockJusticeSeekerTest extends BaseCardTest {

    @Test
    void payingAtBeginningOfCombatPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningPaymentDoesNotPutCounter() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyCreaturesYouControlCanBeTargeted() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    void creaturesYouControlWithCountersHaveWard() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        putCounterOn(protectedCreature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unprotectedCreature.getId());
        harness.passBothPriorities();

        assertThat(unprotectedCreature.getMarkedDamage()).isEqualTo(2);
    }

    private void putCounterOn(Permanent target) {
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
