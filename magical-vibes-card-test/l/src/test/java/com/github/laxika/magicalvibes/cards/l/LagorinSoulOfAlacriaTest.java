package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.c.ClamorousIronclad;
import com.github.laxika.magicalvibes.cards.e.EngineRat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LagorinSoulOfAlacria.class, BrightfieldGlider.class, EngineRat.class, ClamorousIronclad.class})
class LagorinSoulOfAlacriaTest extends BaseCardTest {

    @Test
    @DisplayName("Saddling Lagorin taps another creature and marks it saddled")
    void saddleTapsAnotherCreature() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent helper = addCreatureReady(player1, new EngineRat());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lagorin.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled puts a counter on up to two target Mounts or Vehicles")
    void attacksWhileSaddledCountersTwoTargets() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent mount = addCreatureReady(player1, new BrightfieldGlider());
        Permanent vehicle = addCreatureReady(player1, new ClamorousIronclad());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(mount.getId(), vehicle.getId()));
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lagorin's attack trigger may choose zero targets")
    void attackTriggerMayChooseZeroTargets() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent mount = addCreatureReady(player1, new BrightfieldGlider());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lagorin cannot target a permanent without the Mount or Vehicle subtype")
    void cannotTargetOtherPermanent() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent creature = addCreatureReady(player1, new EngineRat());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking without being saddled does not trigger counters")
    void unsaddledAttackDoesNotTrigger() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent mount = addCreatureReady(player1, new BrightfieldGlider());

        declareAttackers(List.of(indexOf(lagorin)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(lagorin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A saddled Lagorin can choose itself as its only target")
    void attackCanTargetItself() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));
        harness.handleMultiplePermanentsChosen(player1, List.of(lagorin.getId()));
        harness.passBothPriorities();

        assertThat(lagorin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lagorin can put counters on an opponent's Mount and uncrewed Vehicle")
    void attackCanTargetOpposingPermanents() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent mount = addCreatureReady(player2, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new ClamorousIronclad());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));
        harness.handleMultiplePermanentsChosen(player1, List.of(mount.getId(), vehicle.getId()));
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lagorin cannot pay its saddle cost by tapping itself")
    void cannotSaddleItself() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lagorin.isTapped()).isFalse();
        assertThat(lagorin.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("A creature with summoning sickness can saddle Lagorin")
    void summoningSickCreatureCanSaddle() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new EngineRat());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(lagorin.isSaddled()).isTrue();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
