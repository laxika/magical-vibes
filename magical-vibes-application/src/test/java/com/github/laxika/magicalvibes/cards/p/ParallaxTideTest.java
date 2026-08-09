package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallaxTideTest extends BaseCardTest {

    private void castAndResolveTide() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ParallaxTide()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Parallax Tide enters with five fade counters")
    void entersWithFiveFadeCounters() {
        castAndResolveTide();

        Permanent tide = findPermanent(player1, "Parallax Tide");
        assertThat(tide.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Parallax Tide removes a fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(tide.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Parallax Tide");
    }

    @Test
    @DisplayName("Parallax Tide sacrifices itself when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        harness.addToBattlefield(player1, new ParallaxTide());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Parallax Tide");
    }

    @Test
    @DisplayName("Parallax Tide exiles a target land and returns it when Tide leaves")
    void exilesLandUntilTideLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(tide.getCounterCount(CounterType.FADE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, tide.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Parallax Tide cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
