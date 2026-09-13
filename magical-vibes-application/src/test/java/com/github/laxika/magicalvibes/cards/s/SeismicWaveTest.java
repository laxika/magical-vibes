package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicWave.class, GrizzlyBears.class, PhyrexianWalker.class})
class SeismicWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the any target and 1 damage to each nonartifact creature the opponent controls")
    void dealsBothDamagesAndSkipsArtifactCreatures() {
        harness.setLife(player2, 20);
        Permanent nonartifactCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifactCreature = addCreatureReady(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(nonartifactCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(artifactCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires the second target to be an opponent")
    void requiresOpponentTarget() {
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
