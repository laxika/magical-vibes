package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaguWildlingTest extends BaseCardTest {

    @Test
    @DisplayName("When Sagu Wildling enters, its controller gains 3 life")
    void entersAndGainsLife() {
        harness.setHand(player1, List.of(new SaguWildling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Omen searches a basic land to hand and shuffles Sagu Wildling into its owner's library")
    void omenSearchesBasicLandAndShuffles() {
        Card saguWildling = new SaguWildling();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(saguWildling));
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).contains(saguWildling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(saguWildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
