package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeveredLegion.class, GrizzlyBears.class, ScatheZombies.class, Juggernaut.class})
class SeveredLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Severed Legion cannot be blocked by non-black non-artifact creatures")
    void cannotBeBlockedByNonBlackNonArtifactCreatures() {
        addCreatureReady(player1, new SeveredLegion());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Severed Legion can be blocked by black creatures")
    void canBeBlockedByBlackCreatures() {
        addCreatureReady(player1, new SeveredLegion());
        addCreatureReady(player2, new ScatheZombies());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Severed Legion can be blocked by artifact creatures")
    void canBeBlockedByArtifactCreatures() {
        addCreatureReady(player1, new SeveredLegion());
        addCreatureReady(player2, new Juggernaut());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Black and artifact creatures can block Severed Legion together")
    void canBeBlockedByBlackAndArtifactCreaturesTogether() {
        addCreatureReady(player1, new SeveredLegion());
        addCreatureReady(player2, new ScatheZombies());
        addCreatureReady(player2, new Juggernaut());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A legal blocker does not allow a non-black non-artifact creature to block")
    void eachBlockerMustSatisfyFear() {
        addCreatureReady(player1, new SeveredLegion());
        addCreatureReady(player2, new ScatheZombies());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }
}