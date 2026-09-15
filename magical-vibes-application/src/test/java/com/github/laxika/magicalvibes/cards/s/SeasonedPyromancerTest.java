package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonedPyromancer.class, Forest.class, GrizzlyBears.class})
class SeasonedPyromancerTest extends BaseCardTest {

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
