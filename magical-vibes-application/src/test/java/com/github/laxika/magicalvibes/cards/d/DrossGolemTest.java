package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChitteringRats;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TangleSpider;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrossGolem.class, Swamp.class, TangleSpider.class, DarksteelGargoyle.class,
        ChitteringRats.class})
class DrossGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Swamps reduces the generic mana cost even when Swamps are tapped")
    void affinityForSwampsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();
        }
        harness.setHand(player1, List.of(new DrossGolem()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only Swamps controlled by the spell's controller")
    void affinityCountsOnlyControlledSwamps() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Swamp());
        }
        harness.setHand(player1, List.of(new DrossGolem()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts Swamps rather than other permanents")
    void affinityCountsSwampsRatherThanOtherPermanents() {
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new TangleSpider());
        }
        harness.setHand(player1, List.of(new DrossGolem()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fear stops a nonblack, nonartifact creature from blocking Dross Golem")
    void fearStopsNonblackNonartifactCreature() {
        addCreatureReady(player1, new DrossGolem());
        addCreatureReady(player2, new TangleSpider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Dross Golem (fear)");
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block Dross Golem")
    void fearAllowsArtifactCreatureToBlock() {
        addCreatureReady(player1, new DrossGolem());
        addCreatureReady(player2, new DarksteelGargoyle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Fear allows a black creature to block Dross Golem")
    void fearAllowsBlackCreatureToBlock() {
        addCreatureReady(player1, new DrossGolem());
        addCreatureReady(player2, new ChitteringRats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }
}
