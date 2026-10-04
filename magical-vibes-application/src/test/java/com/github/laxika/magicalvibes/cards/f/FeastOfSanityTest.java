package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Aeromoeba;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeastOfSanity.class, ZuranEnchanter.class, GrizzlyBears.class, Aeromoeba.class})
class FeastOfSanityTest extends BaseCardTest {

    @Test
    void discardDealsDamageToTargetCreatureAndGainsLife() {
        readyDiscarder();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        discardOneCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void discardCanDealDamageToTargetPlayerAndGainsLife() {
        readyDiscarder();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        discardOneCard();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void discardCostTriggersAndCanTargetController() {
        readyCostDiscarder();
        harness.setLife(player1, 20);

        payDiscardCost();

        harness.assertInGraveyard(player1, "Aeromoeba");
        harness.assertLife(player1, 20);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new FeastOfSanity());
        addCreatureReady(player2, new Aeromoeba());
        harness.setHand(player2, List.of(new Aeromoeba()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Aeromoeba");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalTargetPreventsLifeGainAsWellAsDamage() {
        readyCostDiscarder();
        Permanent target = addCreatureReady(player2, new Aeromoeba());
        harness.setLife(player1, 20);

        payDiscardCost();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void triggerResolvesAfterEnchantmentLeavesBattlefield() {
        readyCostDiscarder();
        Permanent feast = findPermanent(player1, "Feast of Sanity");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        payDiscardCost();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(feast);
        gd.playerGraveyards.get(player1.getId()).add(feast.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void readyCostDiscarder() {
        addCreatureReady(player1, new Aeromoeba());
        harness.addToBattlefield(player1, new FeastOfSanity());
        harness.setHand(player1, List.of(new Aeromoeba()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void payDiscardCost() {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
    }

    private void readyDiscarder() {
        addCreatureReady(player1, new ZuranEnchanter());
        harness.addToBattlefield(player1, new FeastOfSanity());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void discardOneCard() {
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
