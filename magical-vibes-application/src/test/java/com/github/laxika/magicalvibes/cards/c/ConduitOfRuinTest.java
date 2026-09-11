package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EmrakulTheAeonsTorn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanCloudscraper;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConduitOfRuin.class,
        EmrakulTheAeonsTorn.class,
        KrosanCloudscraper.class,
        GrizzlyBears.class,
        MyrRetriever.class
})
class ConduitOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("The cast trigger may put an eligible colorless creature on top of the library")
    void castTriggerSearchesEligibleCreature() {
        Card eligible = new EmrakulTheAeonsTorn();
        Card colored = new KrosanCloudscraper();
        Card tooSmall = new GrizzlyBears();
        castConduit(eligible, colored, tooSmall);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(eligible);

        gs.handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isSameAs(eligible);
    }

    @Test
    @DisplayName("Declining the cast trigger does not search")
    void decliningCastTriggerDoesNotSearch() {
        Card eligible = new EmrakulTheAeonsTorn();
        castConduit(eligible);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    @DisplayName("Only the first creature spell each turn gets the cost reduction")
    void onlyFirstCreatureSpellEachTurnIsReduced() {
        addCreatureReady(player1, new ConduitOfRuin());
        harness.setHand(player1, List.of(new MyrRetriever(), new MyrRetriever()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castConduit(Card... library) {
        harness.setHand(player1, List.of(new ConduitOfRuin()));
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
