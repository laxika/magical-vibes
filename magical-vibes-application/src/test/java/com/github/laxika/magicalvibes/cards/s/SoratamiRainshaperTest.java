package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiRainshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and grants shroud to a creature you control")
    void grantsShroudToOwnCreature() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, bears.getId());

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("A creature an opponent controls is an illegal target")
    void rejectsOpponentCreature() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SoratamiRainshaper());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        // Empty the hand so the returned Island cannot push player1 over the cleanup-step hand limit.
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Rainshaper"), 0, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
