package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.c.ConquerorsGalleon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LagorinSoulOfAlacriaTest extends BaseCardTest {

    @Test
    @DisplayName("Saddling Lagorin taps another creature and marks it saddled")
    void saddleTapsAnotherCreature() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent vehicle = addReadyVehicle(player1);
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, mount.getId());
        harness.handlePermanentChosen(player1, vehicle.getId());
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
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lagorin cannot target a permanent without the Mount or Vehicle subtype")
    void cannotTargetOtherPermanent() {
        Permanent lagorin = addCreatureReady(player1, new LagorinSoulOfAlacria());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        lagorin.setSaddled(true);

        declareAttackers(List.of(indexOf(lagorin)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVehicle(Player player) {
        Permanent vehicle = new Permanent(new ConquerorsGalleon());
        vehicle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vehicle);
        return vehicle;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
