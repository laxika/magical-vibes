package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggManiac.class, Shock.class, HonorGuard.class, JaceBeleren.class})
class MoggManiacTest extends BaseCardTest {

    @Test
    void nonCombatDamageDealsTheSameAmountToOpponent() {
        harness.addToBattlefield(player2, new MoggManiac());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mogg Maniac"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Mogg Maniac");
    }

    @Test
    void combatDamageDealsTheSameAmountToOpponent() {
        Permanent attacker = addCreatureReady(player1, new HonorGuard());
        attacker.setAttacking(true);

        Permanent maniac = addCreatureReady(player2, new MoggManiac());
        maniac.setBlocking(true);
        maniac.addBlockingTarget(0);

        harness.setLife(player1, 20);
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player2, "Mogg Maniac");
    }

    @Test
    void canTargetAnOpponentsPlaneswalker() {
        harness.addToBattlefield(player2, new MoggManiac());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mogg Maniac"));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(jace.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player2, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void canTargetEitherPlayersPlaneswalker() {
        harness.addToBattlefield(player2, new MoggManiac());
        Permanent ownJace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        ownJace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opponentsJace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        opponentsJace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mogg Maniac"));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(ownJace.getId(), opponentsJace.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player2, ownJace.getId());
        harness.passBothPriorities();

        assertThat(ownJace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }
}
