package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpoilsOfWar.class, BalduvianBears.class, SnowCoveredMountain.class,
        SoldeviSimulacrum.class, ZuranOrb.class})
class SpoilsOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes counters equal to the artifacts and creatures in the opponent's graveyard")
    void distributesCountersAmongTwoCreatures() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        // 3 creature cards in the opponent's graveyard -> X = 3, split 2/1.
        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts artifact and artifact creature cards in the opponent's graveyard once each")
    void countsArtifactAndArtifactCreatureCards() {
        harness.setGraveyard(player2, List.of(new ZuranOrb(), new SoldeviSimulacrum()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores non-artifact, non-creature cards and the caster's own graveyard")
    void countsOnlyOpponentArtifactsAndCreatures() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new SnowCoveredMountain()));
        harness.setGraveyard(player1, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        // Only the opponent's single creature card counts: the land and player1's graveyard don't.
        harness.castSorcery(player1, 0, Map.of(bears.getId(), 1));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Assignments must sum to the graveyard count")
    void assignmentsMustSumToGraveyardCount() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires at least one counter for every declared target")
    void rejectsZeroCounterAssignment() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(first.getId(), 0, second.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no target creatures when the distribution total is positive")
    void canChooseNoTargets() {
        harness.setGraveyard(player2, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can target a creature controlled by the opponent")
    void canTargetOpponentCreature() {
        harness.setGraveyard(player2, List.of(new BalduvianBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, Map.of(target.getId(), 1));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires the graveyard count in addition to the black mana")
    void requiresManaForGraveyardCount() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(target.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.setGraveyard(player2, List.of(new BalduvianBears()));
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(mountain.getId(), 1))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Total is locked at cast time, not recomputed at resolution")
    void locksGraveyardCountAtCastTime() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2));

        // Empty the opponent's graveyard after the spell is on the stack.
        harness.getGameData().playerGraveyards.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Skips a target that left the battlefield, keeping the rest")
    void partiallyResolvesWhenATargetLeaves() {
        harness.setGraveyard(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent staying = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SpoilsOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, Map.of(staying.getId(), 1, leaving.getId(), 1));

        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getId().equals(leaving.getId()));

        harness.passBothPriorities();

        assertThat(staying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
