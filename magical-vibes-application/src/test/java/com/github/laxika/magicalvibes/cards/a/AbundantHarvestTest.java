package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbundantHarvest.class, Forest.class, GrizzlyBears.class, Mountain.class})
class AbundantHarvestTest extends BaseCardTest {

    @Test
    void choosesLandAndPutsFirstLandIntoHand() {
        Card nonland = new GrizzlyBears();
        Card land = new Forest();
        Card untouched = new Mountain();
        castWithLibrary(nonland, land, untouched);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonland, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonland, untouched);
    }

    @Test
    void choosesNonlandAndPutsFirstNonlandIntoHand() {
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        Card untouched = new Mountain();
        castWithLibrary(land, nonland, untouched);

        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, land);
    }

    @Test
    void putsEntireLibraryOnBottomWhenChosenKindIsMissing() {
        Card firstLand = new Forest();
        Card secondLand = new Mountain();
        castWithLibrary(firstLand, secondLand);

        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
    }

    private void castWithLibrary(Card... library) {
        harness.setHand(player1, List.of(new AbundantHarvest()));
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
