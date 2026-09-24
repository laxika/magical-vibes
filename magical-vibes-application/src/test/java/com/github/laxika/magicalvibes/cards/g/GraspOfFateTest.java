package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraspOfFate.class, GrizzlyBears.class, Forest.class, Naturalize.class})
class GraspOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one nonland permanent per opponent")
    void exilesOneNonlandPermanentPerOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(firstBear.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondBear.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstBear.getId()));
    }

    @Test
    @DisplayName("Exiled permanents return when Grasp of Fate leaves")
    void exiledPermanentsReturnWhenSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(List.of(target.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID graspId = harness.getPermanentId(player1, "Grasp of Fate");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, graspId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by the caster")
    void cannotTargetIllegalPermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsControlledBySameOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private void castAndResolve(List<UUID> targetIds) {
        prepareCast();
        harness.castEnchantment(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GraspOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
