package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousHarpy.class, GreenwoodSentinel.class})
class RavenousHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Ravenous Harpy")
    void sacrificeCreatureAddsCounter() {
        Permanent harpy = addReadyHarpy(player1);
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Ravenous Harpy to its own ability")
    void cannotSacrificeItself() {
        addReadyHarpy(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations accumulate +1/+1 counters")
    void multipleActivationsAccumulateCounters() {
        Permanent harpy = addReadyHarpy(player1);

        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without paying {1}")
    void requiresMana() {
        addCreatureReady(player1, new RavenousHarpy());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, while the counter waits for resolution")
    void sacrificeIsAnActivationCost() {
        Permanent harpy = addReadyHarpy(player1);
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Harpy can sacrifice a tapped, summoning-sick creature")
    void activationDoesNotRequireTappingOrHaste() {
        Permanent harpy = harness.addToBattlefieldAndReturn(player1, new RavenousHarpy());
        harpy.setSummoningSick(true);
        harpy.tap();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        fodder.setSummoningSick(true);
        fodder.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harpy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent harpy = addReadyHarpy(player1);
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a sacrifice excludes the Harpy itself and sacrifices only the chosen creature")
    void multipleCreaturesRequireChoosingAnotherCreature() {
        Permanent harpy = addReadyHarpy(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, harpy.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(harpy, first).doesNotContain(second);
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An ability whose Harpy was sacrificed does not put its counter on another Harpy")
    void removedSourceDoesNotTransferItsCounter() {
        Permanent firstHarpy = addReadyHarpy(player1);
        Permanent secondHarpy = addCreatureReady(player1, new RavenousHarpy());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondHarpy).doesNotContain(firstHarpy);
        harness.assertInGraveyard(player1, "Ravenous Harpy");
        resolveAllTriggers();

        assertThat(secondHarpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyHarpy(Player controller) {
        Permanent harpy = addCreatureReady(controller, new RavenousHarpy());
        harness.addMana(controller, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return harpy;
    }
}
