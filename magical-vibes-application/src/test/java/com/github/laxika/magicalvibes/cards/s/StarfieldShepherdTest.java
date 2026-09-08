package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Savannah;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfieldShepherd.class, Forest.class, GrizzlyBears.class, Memnite.class, Plains.class, Savannah.class})
class StarfieldShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a basic Plains or a creature with mana value 1 or less")
    void etbSearchesForBasicPlainsOrSmallCreature() {
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Savannah(), new GrizzlyBears(), new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Memnite");

        int memniteIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Memnite");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(memniteIndex));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Memnite");
    }

    @Test
    @DisplayName("Warp casts Starfield Shepherd for {1}{W} and exiles it at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(shepherd.getId())).isNotNull();
    }
}
