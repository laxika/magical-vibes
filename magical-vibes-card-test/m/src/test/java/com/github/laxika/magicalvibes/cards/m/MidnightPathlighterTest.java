package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.s.SramSeniorEdificer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightPathlighter.class, SolemnSimulacrum.class, SramSeniorEdificer.class})
class MidnightPathlighterTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control can't be blocked by nonlegendary creatures")
    void ownCreatureCannotBeBlockedByNonlegendaryCreature() {
        addCreatureReady(player1, new MidnightPathlighter());
        Permanent attacker = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent blocker = addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creatures");
    }

    @Test
    @DisplayName("Creatures you control can be blocked by legendary creatures")
    void ownCreatureCanBeBlockedByLegendaryCreature() {
        addCreatureReady(player1, new MidnightPathlighter());
        Permanent attacker = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent blocker = addCreatureReady(player2, new SramSeniorEdificer());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The restriction does not affect an opponent's creatures")
    void opponentCreatureIsNotRestricted() {
        addCreatureReady(player1, new MidnightPathlighter());
        Permanent blocker = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent attacker = addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("One or more creatures dealing combat damage ventures once")
    void allyCombatDamageMakesControllerVentureOnce() {
        addCreatureReady(player1, new MidnightPathlighter());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pathlighterItselfCannotBeBlockedByNonlegendaryCreature() {
        addCreatureReady(player1, new MidnightPathlighter());
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creatures");
    }

    @Test
    void anotherCreatureCanTriggerVentureWhilePathlighterDoesNotAttack() {
        addCreatureReady(player1, new MidnightPathlighter());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void opponentCombatDamageDoesNotTriggerVenture() {
        addCreatureReady(player1, new MidnightPathlighter());
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDungeonProgress).isEmpty();
    }
}
