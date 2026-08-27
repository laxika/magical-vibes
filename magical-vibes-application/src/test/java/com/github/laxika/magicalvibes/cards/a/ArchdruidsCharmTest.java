package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchdruidsCharm.class, Forest.class, GrizzlyBears.class, FountainOfYouth.class})
class ArchdruidsCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a searched land onto the battlefield tapped")
    void searchesLandToBattlefieldTapped() {
        Forest forest = new Forest();
        castWithLibrary(forest);

        chooseCard(forest);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a searched creature into hand")
    void searchesCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castWithLibrary(bears);

        chooseCard(bears);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(bears));
    }

    @Test
    @DisplayName("Counters a creature and uses its new power to fight")
    void counterThenFightUsesUpdatedPower() {
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBears);
        harness.addToBattlefield(player2, opposingBears);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 1, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.addToBattlefield(player2, fountain);
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();

        harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Fountain of Youth")));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fountain);
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    private void addGreenMana() {
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    private void castWithLibrary(Card card) {
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new ArchdruidsCharm()));
        addGreenMana();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private void chooseCard(Card card) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int index = search.params().cards().indexOf(card);
        harness.handleCardChosen(player1, index);
    }
}
