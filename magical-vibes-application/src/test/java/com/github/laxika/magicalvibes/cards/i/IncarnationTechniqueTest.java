package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IncarnationTechnique.class, GrizzlyBears.class, Shock.class})
class IncarnationTechniqueTest extends BaseCardTest {

    @Test
    void decliningDemonstrateStillMillsAndReturnsACreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, fiveShocks());
        giveIncarnationTechnique();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
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

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
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
