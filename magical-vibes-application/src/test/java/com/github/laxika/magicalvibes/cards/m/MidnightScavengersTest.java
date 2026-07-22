package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightScavengersTest extends BaseCardTest {

    private void castMidnightScavengers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MidnightScavengers()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a MV≤3 creature card from graveyard to hand")
    void etbReturnsLowManaCreatureToHand() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(elves.getId());

        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("MV 3 creature is a legal target; MV 4+ and non-creatures are not")
    void onlyCreaturesWithManaValueAtMost3AreValid() {
        GrizzlyBears bears = new GrizzlyBears(); // MV 2
        HillGiant giant = new HillGiant(); // MV 4
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(bears, giant, scimitar));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Optional return can be declined")
    void returnCanBeDeclined() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        castMidnightScavengers();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Empty graveyard produces no graveyard choice")
    void emptyGraveyardNoChoice() {
        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Midnight Scavengers"));
    }
}
