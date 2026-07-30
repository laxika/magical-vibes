package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlessingsOfNatureTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlessingsOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Distributes four +1/+1 counters as announced among several creatures")
    void distributesFourCountersAmongThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1, third.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("All four counters may go on a single creature")
    void allFourOnOneCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 4));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Assignments must sum to four")
    void assignmentsMustSumToFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(mountain.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skips a target that left the battlefield, keeping the rest")
    void skipsTargetThatLeftTheBattlefield() {
        Permanent staying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(staying.getId(), 2, leaving.getId(), 2));

        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getId().equals(leaving.getId()));

        harness.passBothPriorities();

        assertThat(staying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Drawing it as the first card this turn offers a miracle reveal")
    void firstDrawOffersMiracleReveal() {
        BlessingsOfNature blessings = new BlessingsOfNature();
        harness.setLibrary(player1, List.of(blessings));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(blessings.getId()));
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new BlessingsOfNature()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
