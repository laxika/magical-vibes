package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiRainshaper.class, Island.class, WanderingOnes.class})
class SoratamiRainshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and grants shroud to a creature you control")
    void grantsShroudToOwnCreature() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId());

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");

        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("A creature an opponent controls is an illegal target")
    void rejectsOpponentCreature() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target Soratami Rainshaper itself")
    void canTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, source.getId());
        harness.passBothPriorities();

        assertThat(source.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Cannot use an opponent's land to pay the return cost")
    void cannotUseOpponentsLandForCost() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player2, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Cannot activate without three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        // Empty the hand so the returned Island cannot push player1 over the cleanup-step hand limit.
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
