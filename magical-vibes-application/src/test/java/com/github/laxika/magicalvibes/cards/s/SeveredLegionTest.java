package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.w.WallOfSpears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeveredLegion.class, GrizzlyBears.class, ScatheZombies.class, WallOfSpears.class, DrossCrocodile.class, Juggernaut.class})
class SeveredLegionTest extends BaseCardTest {


    @Test
    @DisplayName("Severed Legion cannot be blocked by non-black non-artifact creatures")
    void cannotBeBlockedByNonBlackNonArtifactCreatures() {
        Permanent attacker = addCreatureReady(player1, new SeveredLegion());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Severed Legion can be blocked by black creatures")
    void canBeBlockedByBlackCreatures() {
        Permanent attacker = addCreatureReady(player1, new SeveredLegion());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ScatheZombies());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Severed Legion can be blocked by artifact creatures")
    void canBeBlockedByArtifactCreatures() {
        Permanent attacker = addCreatureReady(player1, new SeveredLegion());
        attacker.setAttacking(true);

        addCreatureReady(player2, new WallOfSpears());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
