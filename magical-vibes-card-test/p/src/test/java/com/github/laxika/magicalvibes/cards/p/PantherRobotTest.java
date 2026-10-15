package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PantherRobot.class, Spellbook.class})
class PantherRobotTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces Panther Robot's generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new PantherRobot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by Panther Robot's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new PantherRobot()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity allows a free cast when artifact count exceeds the mana cost")
    void excessArtifactsAllowFreeCast() {
        for (int i = 0; i < 11; i++) {
            harness.addToBattlefield(player1, new PantherRobot());
        }
        harness.setHand(player1, List.of(new PantherRobot()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(12);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts tapped artifact creatures")
    void tappedArtifactCreaturesReduceCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefieldAndReturn(player1, new PantherRobot()).tap();
        }
        harness.setHand(player1, List.of(new PantherRobot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity ignores artifact cards in hand, graveyard, and exile")
    void artifactsOutsideBattlefieldDoNotReduceCost() {
        harness.setHand(player1, List.of(new PantherRobot(), new PantherRobot()));
        harness.setGraveyard(player1, List.of(new PantherRobot()));
        harness.setExile(player1, List.of(new PantherRobot()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
