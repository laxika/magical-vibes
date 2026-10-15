package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.c.CoastalDrake;
import com.github.laxika.magicalvibes.cards.g.GerrardsVerdict;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnlistmentOfficer.class, CavesOfKoilos.class, CoastalDrake.class,
        GerrardsVerdict.class, WoodlandChangeling.class, NamelessInversion.class})
class EnlistmentOfficerTest extends BaseCardTest {

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castOfficer() {
        harness.castFromHand(player1, new EnlistmentOfficer(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soldier cards among the top four go to hand and the rest go to the bottom")
    void soldiersGoToHand() {
        Card soldier1 = new EnlistmentOfficer();
        Card soldier2 = new EnlistmentOfficer();
        Card drake = new CoastalDrake();
        Card caves = new CavesOfKoilos();
        Card verdict = new GerrardsVerdict();

        harness.setLibrary(player1, List.of(soldier1, soldier2, drake, caves, verdict));

        castOfficer();
        finishAnyReorder();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(soldier1, soldier2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drake, caves);
        assertThat(deck).contains(drake, caves, verdict);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card nonSoldier1 = new CavesOfKoilos();
        Card nonSoldier2 = new CavesOfKoilos();
        Card nonSoldier3 = new CavesOfKoilos();
        Card nonSoldier4 = new CavesOfKoilos();
        Card deepSoldier = new EnlistmentOfficer();

        harness.setLibrary(player1, List.of(nonSoldier1, nonSoldier2, nonSoldier3, nonSoldier4, deepSoldier));

        castOfficer();
        finishAnyReorder();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepSoldier);
        assertThat(deck).contains(deepSoldier);
    }

    @Test
    @DisplayName("Changeling and noncreature Soldier cards count as Soldier cards")
    void changelingAndNoncreatureSoldierCardsGoToHand() {
        Card changeling = new WoodlandChangeling();
        Card noncreatureSoldier = new NamelessInversion();
        Card verdict = new GerrardsVerdict();

        harness.setLibrary(player1, List.of(changeling, noncreatureSoldier, verdict));

        castOfficer();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling, noncreatureSoldier);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(verdict);
    }

    @Test
    @DisplayName("The controller chooses the order of non-Soldiers below the unrevealed cards")
    void choosesBottomOrder() {
        Card soldier = new EnlistmentOfficer();
        Card drake = new CoastalDrake();
        Card caves = new CavesOfKoilos();
        Card verdict = new GerrardsVerdict();
        Card unrevealed = new EnlistmentOfficer();
        harness.setLibrary(player1, List.of(soldier, drake, caves, verdict, unrevealed));

        castOfficer();

        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(drake, caves, verdict);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, verdict, drake, caves);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(soldier);
    }

    @Test
    @DisplayName("All Soldiers in a library with fewer than four cards go to hand")
    void shortLibraryWithOnlySoldiers() {
        Card first = new EnlistmentOfficer();
        Card second = new EnlistmentOfficer();
        harness.setLibrary(player1, List.of(first, second));

        castOfficer();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library requires no choice and does not draw a card")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        castOfficer();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
