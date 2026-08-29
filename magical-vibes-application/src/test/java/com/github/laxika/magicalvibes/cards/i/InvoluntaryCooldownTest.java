package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoluntaryCooldownTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two artifacts and/or creatures and puts two stun counters on each")
    void tapsAndStunsArtifactsAndCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new InvoluntaryCooldown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a noncreature artifact")
    void tapsAndStunsNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new InvoluntaryCooldown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new InvoluntaryCooldown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
