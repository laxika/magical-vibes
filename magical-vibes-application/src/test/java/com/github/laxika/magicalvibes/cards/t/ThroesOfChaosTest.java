package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThroesOfChaos.class, Forest.class, Mountain.class, HillGiant.class, GrizzlyBears.class})
class ThroesOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade casts the first qualifying card for free and puts skipped cards on the bottom")
    void cascadeCastsFirstCheaperNonland() {
        Mountain skippedLand = new Mountain();
        HillGiant skippedNonland = new HillGiant();
        GrizzlyBears hit = new GrizzlyBears();
        Forest belowHit = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedNonland, hit, belowHit));
        harness.setHand(player1, List.of(new ThroesOfChaos()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, skippedLand, skippedNonland);
    }

    @Test
    @DisplayName("Retrace casts Throes of Chaos from the graveyard by discarding a land")
    void retraceCastsFromGraveyard() {
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.setGraveyard(player1,
                new ArrayList<>(List.of(new ThroesOfChaos(), new Mountain(), new Forest())));
        harness.setHand(player1, new ArrayList<>(List.of(new Mountain())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Throes of Chaos");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
