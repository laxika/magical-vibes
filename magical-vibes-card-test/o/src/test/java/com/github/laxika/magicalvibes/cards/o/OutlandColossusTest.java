package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutlandColossus.class, TimberpackWolf.class})
class OutlandColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 6 puts six +1/+1 counters on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent colossus = addCreatureReady(player1, new OutlandColossus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(colossus.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when it is already renowned")
    void renownOnlyOnce() {
        Permanent colossus = addCreatureReady(player1, new OutlandColossus());
        colossus.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("It can be blocked by a single creature")
    void canBeBlockedByOneCreature() {
        addCreatureReady(player1, new OutlandColossus());
        addCreatureReady(player2, new TimberpackWolf());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("It can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new OutlandColossus());
        addCreatureReady(player2, new TimberpackWolf());
        addCreatureReady(player2, new TimberpackWolf());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Combat damage to a blocker does not cause renown or damage the defending player")
    void blockedCombatDoesNotCauseRenown() {
        Permanent colossus = addCreatureReady(player1, new OutlandColossus());
        addCreatureReady(player2, new TimberpackWolf());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(colossus);
        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(colossus.isRenowned()).isFalse();
    }
}
