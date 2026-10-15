package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.p.PrehistoricTurtlesaurus;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UtromMonitor.class, ArcaneSignet.class})
class UtromMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ArcaneSignet());
        }
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new ArcaneSignet());
        }
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void paysFullCostWithoutArtifacts() {
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Utrom Monitor");
    }

    @Test
    void artifactCreaturesAndTappedArtifactsCountForAffinity() {
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new UtromMonitor()).tap();
        }
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessArtifactsDoNotReduceTheBlueManaRequirement() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new UtromMonitor());
        }
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void artifactsInHandAndGraveyardDoNotReduceCost() {
        harness.setHand(player1, List.of(new UtromMonitor(), new UtromMonitor(), new UtromMonitor()));
        harness.setGraveyard(player1, List.of(new UtromMonitor(), new UtromMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @CardUsed(PrehistoricTurtlesaurus.class)
    void nonartifactPermanentsDoNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PrehistoricTurtlesaurus());
        }
        harness.setHand(player1, List.of(new UtromMonitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @CardUsed(PrehistoricTurtlesaurus.class)
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player2, new PrehistoricTurtlesaurus());
        addCreatureReady(player1, new UtromMonitor()).setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockMonitor() {
        addCreatureReady(player2, new UtromMonitor());
        addCreatureReady(player1, new UtromMonitor()).setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
