package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntrepidAdversary.class, CandlegroveWitch.class})
class IntrepidAdversaryTest extends BaseCardTest {

    @Test
    void paysMultipleTimesAndScalesOwnCreatureBoost() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new IntrepidAdversary()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent adversary = findPermanent(player1, "Intrepid Adversary");
        assertThat(adversary.getCounterCount(CounterType.VALOR)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, adversary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, adversary)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void mayDeclineWithoutPuttingCountersOnIt() {
        harness.setHand(player1, List.of(new IntrepidAdversary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent adversary = findPermanent(player1, "Intrepid Adversary");
        assertThat(adversary.getCounterCount(CounterType.VALOR)).isZero();
    }

    @Test
    void paymentCreatesASeparateTriggerBeforeCountersArePlaced() {
        harness.setHand(player1, List.of(new IntrepidAdversary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        Permanent adversary = findPermanent(player1, "Intrepid Adversary");
        assertThat(adversary.getCounterCount(CounterType.VALOR)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(adversary.getCounterCount(CounterType.VALOR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, adversary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adversary)).isEqualTo(2);
    }

    @Test
    void cannotPayWithoutWhiteManaRemaining() {
        harness.setHand(player1, List.of(new IntrepidAdversary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent adversary = findPermanent(player1, "Intrepid Adversary");
        assertThat(adversary.getCounterCount(CounterType.VALOR)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anthemUpdatesForNewCreaturesAndStopsWhenSourceLeaves() {
        Permanent adversary = harness.addToBattlefieldAndReturn(player1, new IntrepidAdversary());
        adversary.setCounterCount(CounterType.VALOR, 2);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(adversary);

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }
}
