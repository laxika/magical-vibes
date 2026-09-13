package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveEntry.class, GrizzlyBears.class, Millstone.class})
class ExplosiveEntryTest extends BaseCardTest {

    @Test
    void destroysArtifactAndPutsCounterOnCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(List.of(artifact.getId(), creature.getId()));

        harness.assertInGraveyard(player2, "Millstone");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayChooseNoTargets() {
        cast(List.of());

        harness.assertInGraveyard(player1, "Explosive Entry");
    }

    @Test
    void requiresTheFirstTargetToBeAnArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveEntry()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ExplosiveEntry()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
