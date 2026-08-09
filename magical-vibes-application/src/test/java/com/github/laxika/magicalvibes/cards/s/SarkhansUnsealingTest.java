package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.e.ElderscaleWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SarkhansUnsealingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature with power 4 through 6 deals 4 damage to any target")
    void mediumPowerCreatureDealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Casting a creature with power 7 or greater damages each opponent and their creatures and planeswalkers")
    void highPowerCreatureDealsMassDamage() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent controllerCreature = addCreatureReady(player1, new GrizzlyBears());
        AjaniOutlandChaperone ajaniCard = new AjaniOutlandChaperone();
        Permanent opponentPlaneswalker = new Permanent(ajaniCard);
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        opponentPlaneswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opponentPlaneswalker);

        harness.setHand(player1, List.of(new ElderscaleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(controllerCreature.getMarkedDamage()).isZero();
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature with power less than 4 does not trigger")
    void lowPowerCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarkhansUnsealing());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
