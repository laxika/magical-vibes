package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawbladeSkinripper.class, GrizzlyBears.class, GhostlyPrison.class})
class SawbladeSkinripperTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndPutsCounterOnItself() {
        Permanent skinripper = harness.addToBattlefieldAndReturn(player1, new SawbladeSkinripper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skinripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificesAnotherEnchantmentAndPutsCounterOnItself() {
        Permanent skinripper = harness.addToBattlefieldAndReturn(player1, new SawbladeSkinripper());
        harness.addToBattlefield(player1, new GhostlyPrison());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skinripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ghostly Prison");
    }

    @Test
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new SawbladeSkinripper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice another creature or enchantment");
    }

    @Test
    void dealsDamageEqualToPermanentsSacrificedThisTurnAtEndStep() {
        harness.addToBattlefield(player1, new SawbladeSkinripper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GhostlyPrison());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerAtEndStepWithoutASacrifice() {
        harness.addToBattlefield(player1, new SawbladeSkinripper());

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
