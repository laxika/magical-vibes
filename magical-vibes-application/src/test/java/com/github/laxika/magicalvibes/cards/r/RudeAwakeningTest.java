package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RudeAwakening.class, Forest.class, Mountain.class, GrizzlyBears.class,
        DrossCrocodile.class, ImprisonedInTheMoon.class})
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

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, opponentMountain)).isFalse();
    }

    @Test
    @DisplayName("Animation mode affects a permanent that is currently a land")
    void animationModeAnimatesPermanentCurrentlyLand() {
        Permanent transformedLand = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(transformedLand.getId());

        assertThat(gqs.isLand(gd, transformedLand)).isTrue();

        cast(new int[]{1}, false);

        assertThat(gqs.isCreature(gd, transformedLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, transformedLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, transformedLand)).isEqualTo(2);
        assertThat(gqs.isLand(gd, transformedLand)).isTrue();
    }

    @Test
    @DisplayName("Animation mode wears off at end of turn")
    void animationModeEndsAtEndOfTurn() {
        Permanent forest = addLand(player1, new Forest());

        cast(new int[]{1}, false);
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Entwine untaps and animates your lands")
    void entwinedResolvesBothModes() {
        Permanent forest = addLand(player1, new Forest());
        forest.tap();

        cast(new int[]{0, 1}, true);

        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
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
