package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.s.SegovianLeviathan;
import com.github.laxika.magicalvibes.cards.s.SerpentWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentOfYawningDepths.class, GrizzlyBears.class, KrakenHatchling.class,
        SegovianLeviathan.class, GiantOctopus.class, SerpentWarrior.class})
class SerpentOfYawningDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Sea monsters you control can only be blocked by sea monsters")
    void seaMonstersCanOnlyBeBlockedBySeaMonsters() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent attacker = addCreatureReady(player1, new KrakenHatchling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("Sea monsters can block sea monsters")
    void seaMonstersCanBlockSeaMonsters() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent attacker = addCreatureReady(player1, new KrakenHatchling());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantOctopus());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction applies to all four supported creature types")
    void allSupportedCreatureTypesAreRestricted() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent leviathan = addCreatureReady(player1, new SegovianLeviathan());
        Permanent serpent = addCreatureReady(player1, new SerpentWarrior());
        leviathan.setAttacking(true);
        serpent.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(0, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Krakens, Leviathans, Octopuses, and Serpents");
    }

    @Test
    @DisplayName("Non-sea-monsters you control are unaffected")
    void nonSeaMonstersAreUnaffected() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction does not affect sea monsters controlled by opponents")
    void opponentSeaMonstersAreUnaffected() {
        addCreatureReady(player1, new SerpentOfYawningDepths());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new KrakenHatchling());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
