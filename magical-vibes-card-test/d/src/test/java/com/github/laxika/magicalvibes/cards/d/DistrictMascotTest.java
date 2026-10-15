package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CameraLauncher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistrictMascot.class, Forest.class, CameraLauncher.class})
class DistrictMascotTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter")
    void entersWithCounter() {
        harness.castFromHand(player1, new DistrictMascot(), "{G}");
        harness.passBothPriorities();

        Permanent mascot = findPermanent(player1, "District Mascot");

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, mascot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mascot)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes two +1/+1 counters to destroy an artifact")
    void destroysArtifact() {
        Permanent mascot = addReadyMascot(player1, 3);
        harness.addToBattlefield(player2, new CameraLauncher());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Camera Launcher");
        harness.activateAbility(player1, indexOf(player1, mascot), null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Camera Launcher");
        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent mascot = addReadyMascot(player1, 3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, mascot), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the artifact ability without two +1/+1 counters")
    void needsTwoCounters() {
        Permanent mascot = addReadyMascot(player1, 1);
        harness.addToBattlefield(player2, new CameraLauncher());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Camera Launcher");
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, mascot), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Saddled attack puts a +1/+1 counter on it")
    void saddledAttackPutsCounter() {
        Permanent mascot = addReadyMascot(player1, 1);
        Permanent helper = addReadyMascot(player1, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(player1, mascot), 1, null, null);
        harness.passBothPriorities();

        assertThat(mascot.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();

        declareAttackers(player1, List.of(indexOf(player1, mascot)));
        resolveAllTriggers();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An attack while not saddled does not put a counter on it")
    void attackWhileNotSaddledDoesNotPutCounter() {
        Permanent mascot = addReadyMascot(player1, 1);

        declareAttackers(player1, List.of(indexOf(player1, mascot)));
        resolveAllTriggers();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void artifactAbilityResolvesAfterLastCountersKillMascot() {
        Permanent mascot = addReadyMascot(player1, 2);
        mascot.setSummoningSick(true);
        mascot.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CameraLauncher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, mascot), null, artifact.getId());

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "District Mascot");
        harness.assertOnBattlefield(player1, "Camera Launcher");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Camera Launcher");
    }

    @Test
    void cannotSaddleUsingItselfOrOpponentsCreatures() {
        Permanent mascot = addReadyMascot(player1, 1);
        addReadyMascot(player2, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, mascot), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mascot.isTapped()).isFalse();
        assertThat(mascot.isSaddled()).isFalse();
    }

    @Test
    void summoningSickCreatureCanSaddle() {
        Permanent mascot = addReadyMascot(player1, 1);
        Permanent helper = addReadyMascot(player1, 1);
        helper.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, indexOf(player1, mascot), 1, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(mascot.isSaddled()).isFalse();
        harness.passBothPriorities();
        assertThat(mascot.isSaddled()).isTrue();
    }

    @Test
    void cannotSaddleAtInstantSpeed() {
        Permanent mascot = addReadyMascot(player1, 1);
        Permanent helper = addReadyMascot(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, mascot), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helper.isTapped()).isFalse();
    }

    private Permanent addReadyMascot(Player player, int counters) {
        Permanent mascot = addCreatureReady(player, new DistrictMascot());
        mascot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return mascot;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
