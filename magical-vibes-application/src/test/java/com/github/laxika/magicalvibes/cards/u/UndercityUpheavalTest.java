package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityUpheaval.class, GrizzlyBears.class, Mountain.class})
class UndercityUpheavalTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes counters from your creature graveyard and grants vigilance to your creatures")
    void distributesCountersAndGrantsVigilance() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Locks the creature graveyard count when the spell is cast")
    void locksCreatureGraveyardCountAtCastTime() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2));
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Rejects an opponent's creature as a counter target")
    void rejectsOpponentCreatureTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(opponent.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature as a counter target")
    void rejectsNoncreatureTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(mountain.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters assigned to a target that leaves are not redistributed")
    void doesNotRedistributeCountersFromMissingTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("No vigilance is granted when every target has left the battlefield")
    void doesNotGrantVigilanceWhenAllTargetsLeave() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(target.getId(), 1));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A target gained by an opponent receives no counters")
    void skipsTargetNowControlledByOpponent() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(stolen.getId(), 2, remaining.getId(), 1));
        gd.playerBattlefields.get(player1.getId()).remove(stolen);
        gd.playerBattlefields.get(player2.getId()).add(stolen);
        harness.passBothPriorities();

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, stolen, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("No effects happen when an opponent gains control of the sole target")
    void doesNotResolveWhenSoleTargetChangesController() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(stolen.getId(), 1));
        gd.playerBattlefields.get(player1.getId()).remove(stolen);
        gd.playerBattlefields.get(player2.getId()).add(stolen);
        harness.passBothPriorities();

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain vigilance")
    void vigilanceDoesNotApplyToLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A positive counter total requires a target")
    void rejectsNoTargetsWhenCountersMustBeDistributed() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("All counters must be distributed")
    void rejectsIncompleteCounterDistribution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(creature.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each target must receive at least one counter")
    void rejectsZeroCounterAssignment() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }
    private void prepareCast() {
        harness.setHand(player1, List.of(new UndercityUpheaval()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
