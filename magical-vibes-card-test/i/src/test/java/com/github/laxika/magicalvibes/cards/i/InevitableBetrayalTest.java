package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({InevitableBetrayal.class, GrizzlyBears.class, Island.class, Unsummon.class, Divination.class, Forest.class})
class InevitableBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Inevitable Betrayal with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        InevitableBetrayal card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspended Inevitable Betrayal steals a creature from the target opponent's library")
    void suspendedSpellStealsCreatureFromTargetLibrary() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Unsummon(), new Island()));
        suspendCard();
        resolveSuspendedSpell();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting("name").containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Unsummon", "Island");
    }

    @Test
    @DisplayName("Inevitable Betrayal offers only opponents as targets")
    void offersOnlyOpponentsAsTargets() {
        suspendCard();
        resolveSuspendedSpellUntilTargetChoice();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());
    }

    private InevitableBetrayal suspendCard() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        InevitableBetrayal card = new InevitableBetrayal();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void resolveSuspendedSpell() {
        resolveSuspendedSpellUntilTargetChoice();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    private void resolveSuspendedSpellUntilTargetChoice() {
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
    }
    @Test
    @DisplayName("A suspended free cast searches an opponent's library for a creature")
    void suspendedCastStealsCreatureFromOpponentsLibrary() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Divination(), new Forest()));

        suspendCard();
        resolveSuspendedSpell();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Only the owner's upkeep removes a time counter, when its trigger resolves")
    void timeCounterRemovedOnlyWhenOwnersUpkeepTriggerResolves() {
        InevitableBetrayal card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card exiled without time counters")
    void decliningSuspendCastLeavesCardExiled() {
        InevitableBetrayal card = suspendCard();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search may fail to find even when a creature is present")
    void mayFailToFindCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        Island land = new Island();
        harness.setLibrary(player2, List.of(creature, land));
        suspendCard();
        resolveSuspendedSpell();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gameLogContains(player2.getUsername() + "'s library is shuffled.")).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library with no creature finishes resolution without a selection")
    void libraryWithoutCreaturesFinishesResolution() {
        Island land = new Island();
        harness.setLibrary(player2, List.of(land));
        suspendCard();
        resolveSuspendedSpell();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library finishes resolution")
    void emptyLibraryFinishesResolution() {
        harness.setLibrary(player2, List.of());
        suspendCard();
        resolveSuspendedSpell();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature enters untapped and summoning sick, with its owner unchanged")
    void stolenCreatureRetainsOwnerAndEntersNormally() {
        GrizzlyBears creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature));
        suspendCard();
        resolveSuspendedSpell();
        harness.handleCardChosen(player1, 0);

        var permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(permanent.getCard()).isSameAs(creature);
        assertThat(permanent.isTapped()).isFalse();
        assertThat(permanent.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gameLogContains(player2.getUsername() + "'s library is shuffled.")).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

}
