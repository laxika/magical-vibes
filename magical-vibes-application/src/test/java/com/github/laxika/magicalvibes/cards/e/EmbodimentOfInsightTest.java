package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StalkingDrone;
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

@CardUsed({EmbodimentOfInsight.class, Forest.class, StalkingDrone.class})
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
        Permanent ownDrone = harness.addToBattlefieldAndReturn(player1, new StalkingDrone());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        assertThat(gqs.hasKeyword(gd, ownDrone, Keyword.VIGILANCE)).isFalse();

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId()).doesNotContain(ownDrone.getId(), opponentForest.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownDrone.getId()))
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

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }

    @Test
    void enteringLandCanAttackWithoutTapping() {
        Permanent embodiment = addEmbodiment();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent forest = findPermanent(player1, "Forest");
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareAttackers(gd, player1,
                        List.of(gd.playerBattlefields.get(player1.getId()).indexOf(forest))));

        assertThat(forest.isAttacking()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, embodiment, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void opponentLandEntryDoesNotTriggerLandfall() {
        addEmbodiment();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void animationSurvivesSourceLeavingButVigilanceDoesNot() {
        Permanent embodiment = addEmbodiment();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, embodiment));

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addEmbodiment() {
        return harness.addToBattlefieldAndReturn(player1, new EmbodimentOfInsight());
    }
}
