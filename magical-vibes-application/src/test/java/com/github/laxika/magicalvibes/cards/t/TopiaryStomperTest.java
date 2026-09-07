package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopiaryStomper.class, Forest.class, GrizzlyBears.class})
class TopiaryStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering searches for a basic land and puts it onto the battlefield tapped")
    void enteringSearchesForTappedBasicLand() {
        Forest forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new TopiaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(forest);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        Permanent forestPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(forestPermanent.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Cannot attack with fewer than seven lands")
    void cannotAttackWithFewerThanSevenLands() {
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 6);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with seven lands")
    void canAttackWithSevenLands() {
        addCreatureReady(player1, new TopiaryStomper());
        addCreatureReady(player2, new GrizzlyBears());
        addForests(player1, 7);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block with fewer than seven lands")
    void cannotBlockWithFewerThanSevenLands() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 6);

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with seven lands")
    void canBlockWithSevenLands() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 7);

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
