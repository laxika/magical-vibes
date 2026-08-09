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

class ParallaxWaveTest extends BaseCardTest {

    private void castAndResolveWave() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ParallaxWave()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Parallax Wave enters with five fade counters")
    void entersWithFiveFadeCounters() {
        castAndResolveWave();

        Permanent wave = findPermanent(player1, "Parallax Wave");
        assertThat(wave.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Parallax Wave removes a fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wave.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Parallax Wave sacrifices itself when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        harness.addToBattlefield(player1, new ParallaxWave());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Parallax Wave exiles a target creature and returns it when Wave leaves")
    void exilesCreatureUntilWaveLeaves() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(wave.getCounterCount(CounterType.FADE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wave.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Parallax Wave cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
