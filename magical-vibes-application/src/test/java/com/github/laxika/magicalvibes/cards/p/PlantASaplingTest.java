package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FullyGrownTreefolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlantASapling.class, FullyGrownTreefolk.class, Forest.class, GrizzlyBears.class})
class PlantASaplingTest extends BaseCardTest {

    @Test
    @DisplayName("searches for a basic land and shuffles itself into its owner's library")
    void searchesForBasicLandAndShufflesItself() {
        PlantASapling plant = new PlantASapling();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(plant));
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).contains(plant).doesNotContain(forest);
    }

    @Test
    @DisplayName("shuffles itself into its owner's library when no basic land is found")
    void shufflesItselfWhenNoBasicLandIsFound() {
        PlantASapling plant = new PlantASapling();
        harness.setHand(player1, List.of(plant));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(plant);
    }

    @Test
    @DisplayName("Fully-Grown Treefolk is as large as the lands its controller controls")
    void powerAndToughnessEqualControlledLands() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new FullyGrownTreefolk());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(2);
    }
}
