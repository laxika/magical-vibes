package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorridorMonitor.class, Aeolipile.class, GrizzlyBears.class})
class CorridorMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and untaps a tapped creature you control")
    void untapsCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        castCorridorMonitor(creature);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters and untaps a tapped artifact you control")
    void untapsArtifactYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Aeolipile());
        artifact.tap();
        castCorridorMonitor(artifact);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature you control");
    }

    private void castCorridorMonitor(Permanent target) {
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCorridorMonitorMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
