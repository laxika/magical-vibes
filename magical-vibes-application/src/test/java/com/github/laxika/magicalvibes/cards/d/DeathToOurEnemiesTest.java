package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathToOurEnemies.class, GrizzlyBears.class, RiteOfFlame.class})
class DeathToOurEnemiesTest extends BaseCardTest {

    @Test
    void noncreatureSpellCreatesTappedTreasureAndPlanCounter() {
        Permanent death = harness.addToBattlefieldAndReturn(player1, new DeathToOurEnemies());
        harness.setHand(player1, List.of(new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(death.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    @Test
    void creatureSpellDoesNotCreateTreasureOrAdvancePlan() {
        Permanent death = harness.addToBattlefieldAndReturn(player1, new DeathToOurEnemies());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(death.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void fourthPlanCounterSacrificesAndDealsSevenDividedAmongTwoTargets() {
        Permanent death = harness.addToBattlefieldAndReturn(player1, new DeathToOurEnemies());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(
                new RiteOfFlame(), new RiteOfFlame(), new RiteOfFlame(), new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveSorcery(player1, 0, 0);
            if (i < 3) {
                harness.passBothPriorities();
            }
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(death);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(death.getCard());
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleColorChosen(player1, "3");
        harness.handleColorChosen(player1, "4");

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bear.getCard());
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAdvancePlan() {
        Permanent death = harness.addToBattlefieldAndReturn(player1, new DeathToOurEnemies());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RiteOfFlame()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(death.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void removingPlanCountersDoesNotStopAlreadyTriggeredSacrifice() {
        Permanent death = harness.addToBattlefieldAndReturn(player1, new DeathToOurEnemies());
        death.setCounterCount(CounterType.PLAN, 3);
        harness.setHand(player1, List.of(new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(death.getCounterCount(CounterType.PLAN)).isEqualTo(4);
        death.setCounterCount(CounterType.PLAN, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(death);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(death.getCard());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
    }
}
