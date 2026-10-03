package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyComplication.class, SanitationAutomaton.class})
class DeadlyComplicationTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys target creature")
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Sanitation Automaton");
    }

    @Test
    @DisplayName("The counter mode puts a counter on a suspected creature and may clear suspect")
    void putsCounterAndClearsSuspect() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        creature.setSuspected(true);

        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("Choosing both modes resolves each mode against its target")
    void resolvesBothModes() {
        Permanent creatureToDestroy = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        Permanent suspectedCreature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        suspectedCreature.setSuspected(true);

        cast(new int[]{0, 1}, List.of(creatureToDestroy.getId(), suspectedCreature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Sanitation Automaton");
        assertThat(suspectedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(suspectedCreature.isSuspected()).isFalse();
    }

    @Test
    void mayKeepCreatureSuspected() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        creature.setSuspected(true);

        cast(new int[]{1}, List.of(creature.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isSuspected()).isTrue();
        harness.assertOnBattlefield(player1, "Sanitation Automaton");
    }

    @Test
    void bothModesMayTargetSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        creature.setSuspected(true);

        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Sanitation Automaton");
        harness.assertInGraveyard(player1, "Sanitation Automaton");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterModeRejectsUnsuspectedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterModeRejectsOpponentsSuspectedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        creature.setSuspected(true);

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterModeDoesNothingIfTargetStopsBeingSuspectedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        creature.setSuspected(true);

        castWithoutResolving(new int[]{1}, List.of(creature.getId()));
        creature.setSuspected(false);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Sanitation Automaton");
        harness.assertInGraveyard(player1, "Deadly Complication");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void destroyModeStillResolvesWhenCounterTargetBecomesIllegal() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        Permanent counterTarget = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        counterTarget.setSuspected(true);

        castWithoutResolving(new int[]{0, 1}, List.of(destroyed.getId(), counterTarget.getId()));
        counterTarget.setSuspected(false);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player2, "Sanitation Automaton");
        harness.assertOnBattlefield(player1, "Sanitation Automaton");
        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        castWithoutResolving(modes, targets);
        harness.passBothPriorities();
    }

    private void castWithoutResolving(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new DeadlyComplication()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
    }
}
