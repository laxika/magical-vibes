package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ModelOfUnity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TruthOrConsequences.class, Forest.class, ModelOfUnity.class})
class TruthOrConsequencesTest extends BaseCardTest {

    @Test
    @DisplayName("Truth votes draw one card per truth vote")
    void truthVotesDrawCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        cast();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Consequences votes deal three damage per consequences vote to a random opponent")
    void consequencesVotesDealDamage() {
        cast();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Mixed votes draw for truth and deal damage for consequences")
    void mixedVotesApplyBothResults() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        cast();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 17);
    }

    @Test
    void votesStaySecretAndHaveNoEffectUntilEveryoneHasVoted() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        cast();
        harness.handleXValueChosen(player1, 0);

        assertThat(gameLogContains("votes truth")).isFalse();
        assertThat(gameLogContains("votes consequences")).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleXValueChosen(player2, 1);
        resolveAllTriggers();

        assertThat(gameLogContains("votes truth")).isTrue();
        assertThat(gameLogContains("votes consequences")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 17);
    }

    @Test
    void finishingSecretVotingTriggersModelOfUnityForMatchingVoters() {
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());

        cast();
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 1);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void cast() {
        harness.castFromHand(player1, new TruthOrConsequences(), "{2}{U}{R}");
        harness.passBothPriorities();
    }
}
