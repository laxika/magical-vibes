package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MoonringMirrorTest extends BaseCardTest {

    private UUID addMirror() {
        harness.addToBattlefield(player1, new MoonringMirror());
        return harness.getPermanentId(player1, "Moonring Mirror");
    }

    // The player draws a card; ON_CONTROLLER_DRAWS puts the exile trigger on the stack (CR 603.5).
    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    @Test
    @DisplayName("Drawing a card exiles the top card of the controller's library face down with the mirror")
    void drawExilesTopCardFaceDown() {
        UUID permId = addMirror();
        harness.setHand(player1, new ArrayList<>());
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 3; i++) gd.playerDecks.get(player1.getId()).add(new Forest());

        drawAndResolveTrigger(player1);

        // One card drawn into hand, one exiled face down with the mirror.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent drawing does not trigger the mirror")
    void opponentDrawDoesNotTrigger() {
        UUID permId = addMirror();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("Accepting the upkeep trigger swaps the hand with the cards exiled with the mirror")
    void upkeepSwapsHandWithExiledCards() {
        UUID permId = addMirror();
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 4; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        List<UUID> exiledBefore = gd.getCardsExiledByPermanent(permId).stream().map(Card::getId).toList();
        assertThat(exiledBefore).hasSize(2);

        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new LlanowarElves())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> exiledBefore.contains(c.getId()));
        assertThat(gd.getCardsExiledByPermanent(permId))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves hand and exiled cards untouched")
    void upkeepDeclineKeepsEverythingInPlace() {
        UUID permId = addMirror();
        gd.playerDecks.get(player1.getId()).clear();
        // Exactly enough cards for the seeding draw (one drawn, one exiled) so the library is empty
        // at the upkeep and the draw step adds nothing to the counts under test.
        for (int i = 0; i < 2; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        drawAndResolveTrigger(player1);

        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().matches(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
    }

    @Test
    @DisplayName("With an empty hand the upkeep trigger still returns the exiled cards")
    void upkeepWithEmptyHandStillReturnsExiledCards() {
        UUID permId = addMirror();
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 2; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        drawAndResolveTrigger(player1);

        harness.setHand(player1, new ArrayList<>());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }
}
