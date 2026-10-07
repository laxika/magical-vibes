package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtMonitor.class, OrnithopterOfParadise.class})
class ThoughtMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic cost and the ETB draws two cards")
    void affinityAndEnterTheBattlefieldDraw() {
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise(), new OrnithopterOfParadise()));
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new ThoughtMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Thought Monitor");
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new ThoughtMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity cannot reduce the blue mana requirement even with excess artifacts")
    void affinityDoesNotReduceColoredMana() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new ThoughtMonitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity bottoms out at one blue mana and drawing waits for the enter trigger")
    void excessArtifactsAndDrawTriggerTiming() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise(), new OrnithopterOfParadise()));
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new ThoughtMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Thought Monitor");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Monitor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact spell in hand does not count itself for affinity")
    void thoughtMonitorDoesNotCountItselfBeforeEntering() {
        harness.setHand(player1, List.of(new ThoughtMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
