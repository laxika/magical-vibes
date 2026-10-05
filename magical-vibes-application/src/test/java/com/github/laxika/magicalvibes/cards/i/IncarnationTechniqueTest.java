package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncarnationTechnique.class, GrizzlyBears.class, Shock.class, Counterspell.class})
class IncarnationTechniqueTest extends BaseCardTest {

    @Test
    void decliningDemonstrateStillMillsAndReturnsACreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, fiveShocks());
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        resolveGraveyardChoices();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("A copy of Incarnation Technique")).isFalse();
    }

    @Test
    void acceptedDemonstrateCreatesCopiesForTheCasterAndChosenOpponent() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, fiveShocks());
        harness.setLibrary(player2, fiveShocks());
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveGraveyardChoices();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .contains(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("A copy of Incarnation Technique")))
                .hasSize(2);
    }

    @Test
    void returningAnAvailableCreatureCannotBeDeclined() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, fiveShocks());
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard).contains(creature);
    }

    @Test
    void canReturnANewlyMilledCreatureFromAShortLibrary() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(noncreature, creature));
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        var choice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).get(choice.validIndices().getFirst())).isSameAs(creature);
        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature).doesNotContain(creature);
    }

    @Test
    void resolvesWithoutACreatureAndDoesNotUseTheOpponentsGraveyard() {
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, fiveShocks());
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentCopyResolvesBeforeCasterCopyAndOriginalEachMillFive() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card remainingCard = new Shock();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), remainingCard));
        harness.setLibrary(player2, fiveShocks());
        giveIncarnationTechnique();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        var opponentChoice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(opponentChoice.playerId()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(11);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player2, opponentChoice.validIndices().getFirst());
        resolveAllTriggers();

        var casterChoice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(casterChoice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        var originalChoice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(originalChoice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(firstCreature, secondCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard)
                .containsExactly(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void demonstrateStillCopiesTheSpellAfterTheOriginalIsCountered() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, fiveShocks());
        harness.setLibrary(player2, fiveShocks());
        giveIncarnationTechnique();
        Card original = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveGraveyardChoices();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard).contains(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void giveIncarnationTechnique() {
        harness.setHand(player1, List.of(new IncarnationTechnique()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private List<Card> fiveShocks() {
        return List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
    }

    private void resolveGraveyardChoices() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.GraveyardChoice choice) {
            var player = choice.playerId().equals(player1.getId()) ? player1 : player2;
            harness.handleGraveyardCardChosen(player, choice.validIndices().getFirst());
            resolveAllTriggers();
        }
    }
}
