package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanitationAutomaton.class, GrizzlyBears.class})
class SanitationAutomatonTest extends BaseCardTest {

    @Test
    void entersWithSurveilOne() {
        GameData gd = harness.getGameData();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.setHand(player1, List.of(new SanitationAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void canKeepTopCardWithoutChangingEitherLibrary() {
        Card topCard = new SanitationAutomaton();
        Card nextCard = new SanitationAutomaton();
        Card opponentCard = new SanitationAutomaton();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new SanitationAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard, nextCard);
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId()).doesNotContain(player2.getId());
    }

    @Test
    void surveilsWithAnEmptyLibraryWithoutPrompting() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SanitationAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
