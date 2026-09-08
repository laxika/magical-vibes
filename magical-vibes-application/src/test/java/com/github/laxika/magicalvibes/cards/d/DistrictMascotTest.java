package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistrictMascotTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter")
    void entersWithCounter() {
        harness.setHand(player1, List.of(new DistrictMascot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
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
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.activateAbility(player1, indexOf(player1, mascot), null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
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
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, mascot), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Saddled attack puts a +1/+1 counter on it")
    void saddledAttackPutsCounter() {
        Permanent mascot = addReadyMascot(player1, 1);
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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

    private Permanent addReadyMascot(Player player, int counters) {
        Permanent mascot = new Permanent(new DistrictMascot());
        mascot.setSummoningSick(false);
        mascot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(mascot);
        return mascot;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
