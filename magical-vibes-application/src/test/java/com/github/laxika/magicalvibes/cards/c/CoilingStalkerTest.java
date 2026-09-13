package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingStalker.class, GrizzlyBears.class})
class CoilingStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets a controlled creature without a +1/+1 counter")
    void combatDamagePutsCounterOnEligibleCreature() {
        Permanent stalker = addCreatureReady(player1, new CoilingStalker());
        stalker.setAttacking(true);
        Permanent eligible = addCreatureReady(player1, new GrizzlyBears());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(eligible.getId())
                .doesNotContain(countered.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();

        assertThat(eligible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ninjutsu puts Coiling Stalker onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CoilingStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent stalker = findPermanent(player1, "Coiling Stalker");
        assertThat(stalker.isTapped()).isTrue();
        assertThat(stalker.isAttacking()).isTrue();
        assertThat(stalker.getAttackTarget()).isEqualTo(player2.getId());
    }
}
