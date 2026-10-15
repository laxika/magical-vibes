package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtarkaEfreet.class, ColossodonYearling.class})
class AtarkaEfreetTest extends BaseCardTest {

    @Test
    void turningFaceUpDealsOneDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent efreet = castFaceDown();

        turnFaceUp(efreet);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void turningFaceUpDealsOneDamageToTargetPlayer() {
        Permanent efreet = castFaceDown();

        turnFaceUp(efreet);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void payingMegamorphCostPutsCounterOnEfreetBeforeDamageTriggerResolves() {
        Permanent efreet = castFaceDown();

        turnFaceUp(efreet);

        assertThat(efreet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void efreetCanTargetItselfAndSurvivesItsOneDamageAfterMegamorph() {
        Permanent efreet = castFaceDown();

        turnFaceUp(efreet);
        harness.handlePermanentChosen(player1, efreet.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Atarka Efreet");
        assertThat(efreet.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotTriggerDamageOrAddMegamorphCounter() {
        harness.setHand(player1, List.of(new AtarkaEfreet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Atarka Efreet");
        assertThat(findPermanent(player1, "Atarka Efreet")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new AtarkaEfreet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Atarka Efreet");
    }

    private void turnFaceUp(Permanent efreet) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(efreet));
    }
}
