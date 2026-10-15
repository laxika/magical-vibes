package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BattleMammoth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrostpeakYeti;
import com.github.laxika.magicalvibes.cards.g.GiantOx;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootlessYew.class, BattleMammoth.class, GiantOx.class, FrostpeakYeti.class, Forest.class})
class RootlessYewTest extends BaseCardTest {

    @Test
    @DisplayName("Its death trigger searches for a creature with power or toughness 6 or greater")
    void deathTriggerSearchesByPowerOrToughness() {
        BattleMammoth highPower = new BattleMammoth();
        GiantOx highToughness = new GiantOx();
        FrostpeakYeti tooSmall = new FrostpeakYeti();
        Forest nonCreature = new Forest();
        harness.setLibrary(player1, List.of(highPower, highToughness, tooSmall, nonCreature));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(highPower, highToughness);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsAnyOf(highPower, highToughness);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Its death trigger has no search when no qualifying creature is in the library")
    void deathTriggerDoesNotOfferNonQualifyingCards() {
        FrostpeakYeti tooSmall = new FrostpeakYeti();
        Forest nonCreature = new Forest();
        harness.setLibrary(player1, List.of(tooSmall, nonCreature));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooSmall, nonCreature);
    }

    @Test
    void canChooseCreatureQualifyingOnlyByToughness() {
        GiantOx highToughness = new GiantOx();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(highToughness, remaining));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));
        harness.forceActivePlayer(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(highToughness);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("reveals Giant Ox")
                        && message.contains("puts it into their hand"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWhenQualifyingCreatureExists() {
        BattleMammoth qualifying = new BattleMammoth();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(qualifying, remaining));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));
        harness.forceActivePlayer(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(qualifying);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(qualifying, remaining);
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("chooses not to take a card. Library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchesControllersLibraryDuringOpponentsTurn() {
        BattleMammoth ownCreature = new BattleMammoth();
        GiantOx opposingCreature = new GiantOx();
        harness.setLibrary(player1, List.of(ownCreature));
        harness.setLibrary(player2, List.of(opposingCreature));
        harness.forceActivePlayer(player2);

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(ownCreature, opposingCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
