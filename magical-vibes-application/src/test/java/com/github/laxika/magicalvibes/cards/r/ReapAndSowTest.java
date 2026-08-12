package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReapAndSowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys target land")
    void destroyModeDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new ReapAndSow()));
        addMana(false);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(forest.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Search mode puts a land from the library onto the battlefield")
    void searchModePutsLandOntoBattlefield() {
        prepareCast(false, List.of(new Island(), new GrizzlyBears()));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();
        chooseLibraryCard();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Entwine destroys a land and searches for another land")
    void entwinedResolvesBothModes() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        prepareCast(true, List.of(new Island()));

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(forest.getId()), null);
        harness.passBothPriorities();
        chooseLibraryCard();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature")
    void destroyModeCannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ReapAndSow()));
        addMana(false);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(bears.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast(boolean entwined, List<Card> library) {
        harness.setHand(player1, List.of(new ReapAndSow()));
        harness.setLibrary(player1, library);
        addMana(entwined);
    }

    private void addMana(boolean entwined) {
        harness.addMana(player1, ManaColor.GREEN, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 3);
    }

    private void chooseLibraryCard() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }
}
