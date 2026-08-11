package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismaticUndercurrentsTest extends BaseCardTest {

    @Test
    @DisplayName("Vivid searches for up to one basic land per color among permanents")
    void searchesBasicLandsBasedOnControlledColors() {
        harness.addToBattlefield(player1, new AirElemental());
        Card firstLand = new Forest();
        Card secondLand = new Island();
        Card nonLand = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, secondLand, nonLand));
        castPrismaticUndercurrents();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstLand, secondLand);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
    }

    @Test
    @DisplayName("Prismatic Undercurrents gives its controller one additional land play")
    void grantsControllerAnAdditionalLandPlay() {
        harness.addToBattlefield(player1, new PrismaticUndercurrents());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can play two lands in one turn")
    void controllerCanPlayTwoLands() {
        harness.addToBattlefield(player1, new PrismaticUndercurrents());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .hasSize(2);
    }

    private void castPrismaticUndercurrents() {
        harness.setHand(player1, List.of(new PrismaticUndercurrents()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
