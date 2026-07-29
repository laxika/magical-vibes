package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZirilanOfTheClawTest extends BaseCardTest {

    private void setUpZirilan() {
        harness.addToBattlefield(player1, new ZirilanOfTheClaw());
        findPermanent(player1, "Zirilan of the Claw").setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 3);
    }

    @Test
    @DisplayName("Only Dragon permanent cards are offered by the search")
    void searchOffersOnlyDragons() {
        setUpZirilan();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ShivanDragon(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Shivan Dragon"));
    }

    @Test
    @DisplayName("The found Dragon enters the battlefield with haste")
    void foundDragonEntersWithHaste() {
        setUpZirilan();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ShivanDragon(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Shivan Dragon");
        Permanent dragon = findPermanent(player1, "Shivan Dragon");
        assertThat(dragon.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("The found Dragon is exiled at the beginning of the next end step")
    void foundDragonExiledAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        setUpZirilan();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ShivanDragon(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Shivan Dragon");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shivan Dragon"));
    }

    @Test
    @DisplayName("Finding no Dragon leaves the battlefield unchanged")
    void noDragonFound() {
        setUpZirilan();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
