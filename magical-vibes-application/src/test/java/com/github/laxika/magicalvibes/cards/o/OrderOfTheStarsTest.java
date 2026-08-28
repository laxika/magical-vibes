package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrderOfTheStars.class, GrizzlyBears.class})
class OrderOfTheStarsTest extends BaseCardTest {

    @Test
    void choosesAColorAsItEnters() {
        harness.setHand(player1, List.of(new OrderOfTheStars()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Order of the Stars").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    void preventsCombatDamageFromTheChosenColor() {
        Permanent order = new Permanent(new OrderOfTheStars());
        order.setSummoningSick(false);
        order.setChosenColor(CardColor.GREEN);
        order.setBlocking(true);
        order.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(order);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Order of the Stars");
    }

    @Test
    void takesCombatDamageFromOtherColors() {
        Permanent order = new Permanent(new OrderOfTheStars());
        order.setSummoningSick(false);
        order.setChosenColor(CardColor.RED);
        order.setBlocking(true);
        order.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(order);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Order of the Stars");
        harness.assertInGraveyard(player2, "Order of the Stars");
    }
}
