package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouserMarkIII.class, GolemsHeart.class})
class MouserMarkIIITest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls another artifact")
    void canAttackWithAnotherArtifact() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MouserMarkIII());
        harness.addToBattlefield(player1, new GolemsHeart());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when the only artifact is itself")
    void cannotAttackWhenOnlyArtifactIsItself() {
        addCreatureReady(player1, new MouserMarkIII());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only opponent controls another artifact")
    void cannotAttackWhenOnlyOpponentControlsArtifact() {
        addCreatureReady(player1, new MouserMarkIII());
        harness.addToBattlefield(player2, new GolemsHeart());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Mousers can enable each other to attack together")
    void canAttackTogetherWithAnotherMouser() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MouserMarkIII());
        addCreatureReady(player1, new MouserMarkIII());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can block without controlling another artifact")
    void canBlockWhenOnlyArtifactIsItself() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MouserMarkIII());
        harness.addToBattlefield(player1, new MouserMarkIII());
        addCreatureReady(player2, new MouserMarkIII());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
