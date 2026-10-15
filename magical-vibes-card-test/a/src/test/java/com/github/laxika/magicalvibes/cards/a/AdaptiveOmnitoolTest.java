package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdaptiveOmnitool.class, GrizzlyBears.class, ShimmerMyr.class, Forest.class})
class AdaptiveOmnitoolTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each artifact its controller controls")
    void equippedCreatureGetsArtifactCountBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tool = harness.addToBattlefieldAndReturn(player1, new AdaptiveOmnitool());
        tool.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new ShimmerMyr());
        harness.addToBattlefield(player2, new ShimmerMyr());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking offers an artifact among the top six cards")
    void attackingOffersArtifactAmongTopSix() {
        Card artifact = new ShimmerMyr();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), artifact,
                new GrizzlyBears(), new Forest(), new GrizzlyBears()));

        declareAttackWithAttachedTool();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand and bottoms the rest")
    void choosingArtifactPutsItIntoHand() {
        Card artifact = new ShimmerMyr();
        List<Card> otherCards = List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(artifact, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3), otherCards.get(4)));

        declareAttackWithAttachedTool();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger when the Equipment is not attached")
    void noTriggerWhenUnattached() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).setSummoningSick(false);
        harness.addToBattlefield(player1, new AdaptiveOmnitool());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(),
                new Forest(), new GrizzlyBears(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Adaptive Omnitool"));
    }

    private void declareAttackWithAttachedTool() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent tool = harness.addToBattlefieldAndReturn(player1, new AdaptiveOmnitool());
        tool.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
    }
}
