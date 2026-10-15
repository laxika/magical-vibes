package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BladebackSliver;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FistsOfFlame.class, SnowCoveredForest.class, BladebackSliver.class})
class FistsOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and gives the target creature power equal to cards drawn this turn and trample")
    void drawsAndBoostsBasedOnCardsDrawnThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladebackSliver());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest(), new SnowCoveredForest()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.setHand(player1, List.of(new FistsOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Snow-Covered Forest");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.setHand(player1, List.of(new FistsOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counts the caster's draws when targeting an opponent's creature and locks in the bonus")
    void opposingTargetUsesCasterDrawsAndBonusDoesNotGrow() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BladebackSliver());
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setLibrary(player2, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });
        harness.setHand(player1, List.of(new FistsOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Snow-Covered Forest");

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(target.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not draw when the only target leaves before resolution")
    void illegalTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladebackSliver());
        harness.setHand(player1, List.of(new FistsOfFlame()));
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Fists of Flame");
    }

    @Test
    @DisplayName("Successive spells count each new draw and stack their fixed bonuses")
    void successiveSpellsStackBonuses() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BladebackSliver());
        harness.setHand(player1, List.of(new FistsOfFlame(), new FistsOfFlame()));
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getEffectivePower()).isEqualTo(3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
