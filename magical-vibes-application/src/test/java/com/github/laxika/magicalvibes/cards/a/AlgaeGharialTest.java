package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
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

@CardUsed({AlgaeGharial.class, CruelEdict.class, GrizzlyBears.class})
class AlgaeGharialTest extends BaseCardTest {

    private Permanent gharial() {
        return findPermanent(player1, "Algae Gharial");
    }

    @Test
    @DisplayName("When another creature dies, may put a +1/+1 counter on Algae Gharial (accept)")
    void anotherCreatureDiesAccept() {
        harness.addToBattlefield(player1, new AlgaeGharial());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Grizzly Bears dies → trigger
        resolveAllTriggers(); // resolve trigger → MayEffect prompts

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gharial().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When another creature dies, may decline the counter")
    void anotherCreatureDiesDecline() {
        harness.addToBattlefield(player1, new AlgaeGharial());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gharial().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Algae Gharial does not trigger on its own death (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new AlgaeGharial());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Algae Gharial dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A friendly creature dying also triggers the counter")
    void friendlyCreatureDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AlgaeGharial());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AlgaeGharial());
        other.setMarkedDamage(1);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
    }

    @Test
    @DisplayName("Two creatures dying simultaneously give two independent optional counters")
    void simultaneousDeathsTriggerSeparately() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AlgaeGharial());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AlgaeGharial());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AlgaeGharial());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();
        // The dying Gharials also see each other's death.
        assertThat(gd.stack).hasSize(4);
        while (!gd.stack.isEmpty()) {
            resolveAllTriggers();
            var choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
            assertThat(choice).isNotNull();
            harness.handleMayAbilityChosen(
                    choice.playerId().equals(player1.getId()) ? player1 : player2, true);
        }

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
