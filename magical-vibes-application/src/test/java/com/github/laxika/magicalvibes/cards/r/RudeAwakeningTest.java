package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RudeAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Untap mode untaps all lands you control")
    void untapModeUntapsOwnLands() {
        Permanent ownForest = addLand(player1, new Forest());
        Permanent ownMountain = addLand(player1, new Mountain());
        Permanent opponentForest = addLand(player2, new Forest());
        ownForest.tap();
        ownMountain.tap();
        opponentForest.tap();

        cast(new int[]{0}, false);

        assertThat(ownForest.isTapped()).isFalse();
        assertThat(ownMountain.isTapped()).isFalse();
        assertThat(opponentForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animation mode makes your lands 2/2 creatures until end of turn")
    void animationModeAnimatesOwnLands() {
        Permanent forest = addLand(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMountain = addLand(player2, new Mountain());

        cast(new int[]{1}, false);

        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(2);
        assertThat(forest.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(opponentMountain.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("Entwine untaps and animates your lands")
    void entwinedResolvesBothModes() {
        Permanent forest = addLand(player1, new Forest());
        forest.tap();

        cast(new int[]{0, 1}, true);

        assertThat(forest.isTapped()).isFalse();
        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(2);
        assertThat(forest.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine requires its additional {2}{G}")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new RudeAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card land) {
        return harness.addToBattlefieldAndReturn(player, land);
    }

    private void cast(int[] modes, boolean entwined) {
        harness.setHand(player1, List.of(new RudeAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1 + (entwined ? 1 : 0));
        harness.addMana(player1, ManaColor.COLORLESS, 4 + (entwined ? 2 : 0));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
