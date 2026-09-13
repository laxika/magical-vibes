package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Abundance.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class, Island.class})
class AbundanceTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can replace draw step draw with nonland card")
    void replacesDrawStepDrawWithNonland() {
        harness.addToBattlefield(player1, new Abundance());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, bears, island));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(bears.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(island.getId(), forest.getId());
        assertThat(gd.cardsDrawnThisTurn).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Declining Abundance uses a normal draw")
    void decliningAbundanceDrawsNormally() {
        harness.addToBattlefield(player1, new Abundance());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Choosing land can require bottom reorder for revealed nonlands")
    void choosingLandCanRequireBottomReorder() {
        harness.addToBattlefield(player1, new Abundance());
        Card bears = new GrizzlyBears();
        Card counsel = new CounselOfTheSoratami();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(bears, counsel, forest, island));

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(island.getId(), counsel.getId(), bears.getId());
    }

    @Test
    @DisplayName("If no card of the chosen kind is revealed, all revealed cards go to the bottom")
    void putsAllRevealedCardsOnBottomWhenChosenKindIsMissing() {
        harness.addToBattlefield(player1, new Abundance());
        Card bears = new GrizzlyBears();
        Card counsel = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(bears, counsel));
        harness.setHand(player1, List.of());

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(counsel.getId(), bears.getId());
    }

    @Test
    @DisplayName("An empty library does not cause a loss when Abundance replaces the draw")
    void emptyLibraryDoesNotLoseAfterReplacement() {
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, List.of());

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Each draw from draw-two effect gets its own Abundance choice")
    void drawTwoPromptsPerDraw() {
        harness.addToBattlefield(player1, new Abundance());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(forest, bears, island));
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(bears.getId(), island.getId());
    }
}
