package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWonder;
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

@CardUsed({ThePrideOfHullClade.class, GrizzlyBears.class, WallOfWonder.class, Forest.class})
class ThePrideOfHullCladeTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the total toughness of creatures you control")
    void reducesCostByControlledCreatureToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThePrideOfHullClade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Targeted ability boosts a creature, grants its draw trigger, and lets a defender attack")
    void targetedAbilityWorksForAnotherDefender() {
        addCreatureReady(player1, new ThePrideOfHullClade());
        Permanent wall = addCreatureReady(player1, new WallOfWonder());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, wall.getId());
        harness.passBothPriorities();

        int expectedDraws = wall.getEffectiveToughness();
        assertThat(wall.getPowerModifier()).isEqualTo(1);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20 - wall.getEffectivePower());
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + expectedDraws);
    }

    @Test
    @DisplayName("Targeted attack permission expires at end of turn")
    void attackPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ThePrideOfHullClade());
        addCreatureReady(player1, new WallOfWonder());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null,
                gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new ThePrideOfHullClade());
        Permanent opponentWall = addCreatureReady(player2, new WallOfWonder());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = opponentWall.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
