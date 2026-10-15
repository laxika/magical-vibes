package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowSliver.class, SidewinderSliver.class, AshcoatBear.class})
class ShadowSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Shadow Sliver grants itself shadow")
    void grantsShadowToItself() {
        Permanent shadowSliver = addCreatureReady(player1, new ShadowSliver());

        assertThat(gqs.hasKeyword(gd, shadowSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Grants shadow to another Sliver you control")
    void grantsShadowToAnotherSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Grants shadow to an opponent's Sliver too")
    void grantsShadowToOpponentSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Does not grant shadow to a non-Sliver creature")
    void doesNotGrantShadowToNonSliver() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("A non-shadow creature cannot block a Sliver with shadow")
    void nonShadowCreatureCannotBlockShadowSliver() {
        addCreatureReady(player1, new ShadowSliver());
        addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(shadow)");
    }

    @Test
    @DisplayName("A Sliver with shadow cannot block a non-shadow creature")
    void shadowSliverCannotBlockNonShadowCreature() {
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new ShadowSliver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(shadow)");
    }

    @Test
    @DisplayName("Creatures with shadow can block each other")
    void creaturesWithShadowCanBlockEachOther() {
        addCreatureReady(player1, new ShadowSliver());
        Permanent blocker = addCreatureReady(player2, new ShadowSliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    @Test
    @DisplayName("Slivers lose shadow when the only Shadow Sliver leaves the battlefield")
    void sliversLoseShadowWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new ShadowSliver());
        Permanent ownSliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.SHADOW)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, source));

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.SHADOW)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Another Shadow Sliver keeps granting shadow after one leaves")
    void shadowPersistsUntilLastSourceLeaves() {
        Permanent firstSource = addCreatureReady(player1, new ShadowSliver());
        Permanent secondSource = addCreatureReady(player2, new ShadowSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.SHADOW)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, firstSource));

        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.SHADOW)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, secondSource));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.SHADOW)).isFalse();
    }
}
