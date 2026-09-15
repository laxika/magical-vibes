package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.PendingInteraction;




@CardUsed({SeasonedPyromancer.class, Forest.class, GrizzlyBears.class})
class SeasonedPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates one Elemental for one nonland card discarded")
    void enteringCreatesTokensForNonlandDiscards() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new SeasonedPyromancer());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiling it from the graveyard creates two Elementals")
    void graveyardAbilityCreatesTwoTokens() {
        harness.setGraveyard(player1, List.of(new SeasonedPyromancer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Seasoned Pyromancer");
    }
}

@CardUsed({SeasonedPyromancer.class, Forest.class, GrizzlyBears.class})
class Mh1SeasonedPyromancerTest extends BaseCardTest {

    @Test
    void entersDiscardsTwoDrawsTwoAndCreatesElementalsForNonlands() {
        Forest discardedLand = new Forest();
        GrizzlyBears discardedNonland = new GrizzlyBears();
        Forest drawnOne = new Forest();
        GrizzlyBears drawnTwo = new GrizzlyBears();
        harness.setHand(player1, List.of(new SeasonedPyromancer(), discardedLand, discardedNonland));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedLand, discardedNonland);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void graveyardAbilityExilesSourceAndCreatesTwoElementals() {
        SeasonedPyromancer pyromancer = new SeasonedPyromancer();
        harness.setGraveyard(player1, List.of(pyromancer));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Seasoned Pyromancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(pyromancer);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
    }
}
