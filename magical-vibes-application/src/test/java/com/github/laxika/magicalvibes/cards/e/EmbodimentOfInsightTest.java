package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbodimentOfInsight.class, Forest.class, GrizzlyBears.class})
class EmbodimentOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may animate a land into a hasty 3/3 Elemental")
    void landfallAnimatesLand() {
        addEmbodiment();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Declining landfall leaves the land unchanged")
    void decliningLandfallDoesNothing() {
        addEmbodiment();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Landfall can target only a land you control")
    void landfallTargetIsOwnLand() {
        addEmbodiment();
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId()).doesNotContain(ownBear.getId(), opponentForest.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Landfall animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        addEmbodiment();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addEmbodiment() {
        return harness.addToBattlefieldAndReturn(player1, new EmbodimentOfInsight());
    }
}
