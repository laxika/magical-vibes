package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InevitableBetrayal.class, GrizzlyBears.class, Island.class, Unsummon.class})
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
}
