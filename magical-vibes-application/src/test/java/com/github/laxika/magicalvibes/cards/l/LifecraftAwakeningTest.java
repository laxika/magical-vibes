package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LifecraftAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Puts X counters on and permanently animates a noncreature artifact")
    void putsCountersAndAnimatesNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        cast(2, artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
        assertThat(artifact.isPermanentlyAnimated()).isTrue();
    }

    @Test
    @DisplayName("Puts counters on an artifact creature without animating it")
    void putsCountersOnArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast(2, artifactCreature);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, artifactCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(4);
        assertThat(artifactCreature.isPermanentlyAnimated()).isFalse();
    }

    @Test
    @DisplayName("Puts counters on a noncreature Vehicle without animating it")
    void putsCountersOnVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new RenegadeFreighter());

        cast(2, vehicle);

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(vehicle.isPermanentlyAnimated()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an artifact an opponent controls")
    void cannotTargetOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private void cast(int xValue, Permanent target) {
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castInstant(player1, 0, xValue, target.getId());
        harness.passBothPriorities();
    }
}
