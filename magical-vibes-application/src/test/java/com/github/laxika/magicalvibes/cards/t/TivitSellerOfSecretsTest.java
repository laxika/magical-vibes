package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MarneusCalgar;
import com.github.laxika.magicalvibes.cards.m.ModelOfUnity;
import com.github.laxika.magicalvibes.cards.p.PleaForPower;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TivitSellerOfSecrets.class, SwordsToPlowshares.class, PleaForPower.class, MarneusCalgar.class, ModelOfUnity.class})
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

    @Test
    void noAdditionalVoteWhenTivitLeavesBeforeItsTriggerResolves() {
        Permanent tivit = harness.enterBattlefieldAndReturn(player1, new TivitSellerOfSecrets());
        harness.setHand(player1, java.util.List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, tivit.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tivit);
        resolveAllTriggers();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void additionalVoteAppliesToAnotherCardsVote() {
        harness.addToBattlefield(player1, new TivitSellerOfSecrets());
        harness.setHand(player1, java.util.List.of(new PleaForPower()));
        harness.setLibrary(player1, java.util.List.of(
                new TivitSellerOfSecrets(), new TivitSellerOfSecrets(), new TivitSellerOfSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void eachEvidenceVoteCreatesItsClueSeparately() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, java.util.List.of(
                new TivitSellerOfSecrets(), new TivitSellerOfSecrets(), new TivitSellerOfSecrets()));
        castTivit();

        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.VOTE_AGAIN);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(3);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void eachBriberyVoteCreatesItsTreasureSeparately() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, java.util.List.of(
                new TivitSellerOfSecrets(), new TivitSellerOfSecrets(), new TivitSellerOfSecrets()));
        castTivit();

        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.BRIBERY);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.VOTE_AGAIN);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.BRIBERY);
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void finishingCouncilDilemmaTriggersVotingAbilities() {
        harness.addToBattlefield(player1, new ModelOfUnity());
        castTivit();

        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.CONTINUE);
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsTriggerStartsWithOpponentAndGivesOpponentTheTokens() {
        Permanent tivit = harness.enterBattlefieldAndReturn(player2, new TivitSellerOfSecrets());
        resolveAllTriggers();

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);
        assertThat(activeAdditionalVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaAdditionalVoteChoice.VOTE_AGAIN);
        harness.handleListChoice(player2, ChoiceContext.CouncilDilemmaChoice.BRIBERY);
        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CouncilDilemmaChoice.EVIDENCE);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tivit);
        assertThat(countPermanents(player2, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void castTivit() {
        harness.setHand(player1, java.util.List.of(new TivitSellerOfSecrets()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
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
