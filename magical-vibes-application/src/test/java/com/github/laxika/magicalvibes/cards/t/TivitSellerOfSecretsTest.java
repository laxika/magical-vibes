package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TivitSellerOfSecrets.class)
class TivitSellerOfSecretsTest extends BaseCardTest {

    @Test
    void controllerMayVoteAgainAndCreatesTokensForAllVotes() {
        castTivit();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);

        assertThat(activeAdditionalVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.VOTE_AGAIN);

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.BRIBERY);

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void controllerMayDeclineTheAdditionalVote() {
        castTivit();

        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        assertThat(activeAdditionalVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.CONTINUE);

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void combatDamageAlsoStartsTheCouncilDilemma() {
        Permanent tivit = addCreatureReady(player1, new TivitSellerOfSecrets());
        tivit.setAttacking(true);

        resolveCombat();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.CONTINUE);
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    private void castTivit() {
        harness.setHand(player1, java.util.List.of(new TivitSellerOfSecrets()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.CouncilDilemmaChoice.OPTIONS);
        return choice;
    }

    private PendingInteraction.ColorChoice activeAdditionalVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.CouncilDilemmaAdditionalVoteChoice.OPTIONS);
        return choice;
    }
}
