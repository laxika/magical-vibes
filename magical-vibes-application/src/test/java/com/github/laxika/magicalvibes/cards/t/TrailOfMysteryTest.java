package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrailOfMysteryTest extends BaseCardTest {

    @Test
    @DisplayName("A face-down creature entering lets you search for a basic land")
    void faceDownCreatureTriggersBasicLandSearch() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("A creature turned face up gets +2/+2 until end of turn")
    void turnedFaceUpCreatureGetsBoosted() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
        assertThat(tracker.getPowerModifier()).isEqualTo(2);
        assertThat(tracker.getToughnessModifier()).isEqualTo(2);
    }
}
