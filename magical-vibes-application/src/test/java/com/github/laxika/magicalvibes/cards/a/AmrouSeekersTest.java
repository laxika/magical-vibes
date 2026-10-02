package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmrouSeekers.class, AshcoatBear.class, AmrouScout.class, AssemblyWorker.class})
class AmrouSeekersTest extends BaseCardTest {

    @Test
    @DisplayName("Amrou Seekers cannot be blocked by a nonartifact nonwhite creature")
    void cannotBeBlockedByNonartifactNonwhiteCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, seekers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by artifact creatures or white creatures");
    }

    @Test
    @DisplayName("Amrou Seekers can be blocked by a white creature")
    void canBeBlockedByWhiteCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addCreatureReady(player2, new AmrouScout());

        prepareDeclareBlockers();
        declareBlock(blocker, seekers);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Amrou Seekers can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addCreatureReady(player2, new AssemblyWorker());

        prepareDeclareBlockers();
        declareBlock(blocker, seekers);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingSeekers() {
        Permanent seekers = addCreatureReady(player1, new AmrouSeekers());
        seekers.setAttacking(true);
        return seekers;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
