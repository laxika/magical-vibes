package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinAnarchomancer.class, GoblinPiker.class, GrizzlyBears.class, MindStone.class})
class GoblinAnarchomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Red spells you cast cost {1} less")
    void reducesRedSpells() {
        harness.addToBattlefield(player1, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void reducesGreenSpells() {
        harness.addToBattlefield(player1, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Colorless spells do not receive the cost reduction")
    void doesNotReduceColorlessSpells() {
        harness.addToBattlefield(player1, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
