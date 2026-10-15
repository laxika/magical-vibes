package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
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

@CardUsed({ScavengerDrake.class, CruelEdict.class, GrizzlyBears.class, CylianElf.class, MagmaSpray.class})
class ScavengerDrakeTest extends BaseCardTest {

    private Permanent drake() {
        return findPermanent(player1, "Scavenger Drake");
    }

    @Test
    @DisplayName("When another creature dies, may put a +1/+1 counter on Scavenger Drake (accept)")
    void anotherCreatureDiesAccept() {
        harness.addToBattlefield(player1, new ScavengerDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve trigger → MayEffect prompts

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(drake().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When another creature dies, may decline the counter")
    void anotherCreatureDiesDecline() {
        harness.addToBattlefield(player1, new ScavengerDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(drake().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Scavenger Drake does not trigger on its own death (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new ScavengerDrake());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void friendlyCreatureDeathTriggers() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScavengerDrake());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        elf.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    void simultaneousDeathsGiveIndependentChoices() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScavengerDrake());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        friendly.setMarkedDamage(2);
        opposing.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    void exileInsteadOfDeathDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScavengerDrake());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, elf.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        harness.assertNotInGraveyard(player2, "Cylian Elf");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(elf.getCard().getId()));
    }
}
