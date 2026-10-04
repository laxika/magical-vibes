package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.InventorsGoggles;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GearseekerSerpent.class, InventorsGoggles.class})
class GearseekerSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new InventorsGoggles());
        }
        harness.setHand(player1, List.of(new GearseekerSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new InventorsGoggles());
        }
        harness.setHand(player1, List.of(new GearseekerSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The activated ability makes Gearseeker Serpent unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new GearseekerSerpent());
        serpent.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The activated ability's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new GearseekerSerpent());
        serpent.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(serpent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Excess artifacts do not reduce the two blue mana requirement")
    void excessArtifactsDoNotReduceColoredCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new InventorsGoggles());
        }
        harness.setHand(player1, List.of(new GearseekerSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Partial affinity reduces the cost by one per artifact")
    void partialAffinityReduction() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new InventorsGoggles());
        }
        harness.setHand(player1, List.of(new GearseekerSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity does not reduce the activated ability's cost")
    void affinityDoesNotReduceActivationCost() {
        harness.addToBattlefield(player1, new GearseekerSerpent());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new InventorsGoggles());
        }
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick serpent can activate its ability without tapping")
    void tappedSummoningSickSerpentCanActivate() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new GearseekerSerpent());
        serpent.setSummoningSick(true);
        serpent.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(serpent.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(serpent.isTapped()).isTrue();
    }
}
