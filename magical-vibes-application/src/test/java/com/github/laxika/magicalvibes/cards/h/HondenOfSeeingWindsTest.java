package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WearAway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HondenOfSeeingWinds.class, HondenOfLifesWeb.class, WearAway.class})
class HondenOfSeeingWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a Shrine")
    void drawsOneWhenItIsTheOnlyShrine() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws nothing if its only Shrine is destroyed in response")
    void drawsNothingWhenNoShrinesRemain() {
        var source = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Seeing Winds");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Its trigger still draws for remaining Shrines after the source is destroyed")
    void triggerSurvivesSourceRemoval() {
        var source = harness.addToBattlefieldAndReturn(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Seeing Winds");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws a card for each Shrine its controller controls")
    void drawsForEachControlledShrine() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Counts Shrines when the upkeep trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not count Shrines controlled by an opponent")
    void ignoresOpponentControlledShrines() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
