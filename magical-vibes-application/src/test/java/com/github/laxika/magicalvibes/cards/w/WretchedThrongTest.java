package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WretchedThrongTest extends BaseCardTest {

    private void terminate(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Terminate()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
    }

    private void killThrongAndResolveTrigger() {
        harness.addToBattlefield(player1, new WretchedThrong());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        terminate(player1, harness.getPermanentId(player1, "Wretched Throng"));
        harness.passBothPriorities(); // resolve Terminate → dies → trigger on stack
        harness.passBothPriorities(); // resolve trigger → MayEffect prompt
    }

    private void setupLibraryWithThrongs(int throngCount) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < throngCount; i++) {
            deck.add(new WretchedThrong());
        }
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears());
    }

    @Test
    @DisplayName("When Wretched Throng dies, its controller may search the library")
    void deathCreatesMayPrompt() {
        killThrongAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupLibraryWithThrongs(2);
        killThrongAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Accepting may ability offers only cards named Wretched Throng")
    void acceptingMayInitiatesSearch() {
        setupLibraryWithThrongs(2);
        killThrongAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Wretched Throng"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();
    }

    @Test
    @DisplayName("Choosing a Wretched Throng from search puts it into hand")
    void choosingThrongPutsItIntoHand() {
        setupLibraryWithThrongs(2);
        killThrongAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wretched Throng"));
    }

    @Test
    @DisplayName("No copies in library finds nothing and shuffles")
    void noCopiesInLibraryFindsNothing() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        killThrongAndResolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no cards named Wretched Throng"));
    }
}
