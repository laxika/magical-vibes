package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncrementalGrowth.class, WoodlandChangeling.class, Forest.class, NamelessInversion.class})
class IncrementalGrowthTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Distributes 1, 2, and 3 +1/+1 counters across three creatures")
    void distributesCounters() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent c = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, List.of(a.getId(), b.getId(), c.getId()));

        assertThat(a.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(b.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(c.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot choose the same creature for two targets")
    void requiresDistinctTargets() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        UUID aId = a.getId();
        UUID bId = b.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(aId, bId, aId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-creature permanent is an illegal target")
    void rejectsNonCreatureTarget() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        UUID aId = a.getId();
        UUID bId = b.getId();
        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(aId, bId, landId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 4})
    @DisplayName("Requires exactly three targets")
    void requiresExactlyThreeTargets(int targetCount) {
        List<UUID> targetIds = java.util.stream.IntStream.range(0, targetCount)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling()).getId())
                .toList();
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetIds))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Incremental Growth");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Surviving targets retain their assigned counter amounts when targets die")
    void resolvesOnlySurvivingTargets(int removedTargets) {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling()),
                harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling()),
                harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()));
        harness.setHand(player1, List.of(new IncrementalGrowth()));
        addMana(player1);

        harness.castSorcery(player1, 0, creatures.stream().map(Permanent::getId).toList());
        for (int i = 0; i < creatures.size(); i++) {
            if ((removedTargets & (1 << i)) == 0) {
                continue;
            }
            harness.setHand(player2, List.of(new NamelessInversion()));
            harness.addMana(player2, ManaColor.BLACK, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.castAndResolveInstant(player2, 0, creatures.get(i).getId());

            assertThat(gd.playerBattlefields.values().stream().flatMap(List::stream)
                    .map(Permanent::getId)).doesNotContain(creatures.get(i).getId());
            assertThat(gd.stack).hasSize(1);
        }
        harness.passBothPriorities();

        for (int i = 0; i < creatures.size(); i++) {
            assertThat(creatures.get(i).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    .isEqualTo((removedTargets & (1 << i)) != 0 ? 0 : i + 1);
        }
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Incremental Growth");
    }
}
