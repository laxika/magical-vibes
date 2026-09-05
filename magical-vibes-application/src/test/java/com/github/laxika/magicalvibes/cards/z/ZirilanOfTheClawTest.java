package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CatacombDragon;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZirilanOfTheClaw.class, CatacombDragon.class, JungleWurm.class})
class ZirilanOfTheClawTest extends BaseCardTest {

    private void setUpZirilan() {
        addCreatureReady(player1, new ZirilanOfTheClaw());
        harness.addMana(player1, ManaColor.RED, 3);
    }

    @Test
    @DisplayName("Only Dragon permanent cards are offered by the search")
    void searchOffersOnlyDragons() {
        setUpZirilan();
        CatacombDragon dragon = new CatacombDragon();
        JungleWurm wurm = new JungleWurm();
        harness.setLibrary(player1, List.of(dragon, wurm));

        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanent(player1, "Zirilan of the Claw").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(dragon);
    }

    @Test
    @DisplayName("The found Dragon enters the battlefield with haste")
    void foundDragonEntersWithHaste() {
        setUpZirilan();
        CatacombDragon dragon = new CatacombDragon();
        harness.setLibrary(player1, List.of(dragon, new JungleWurm()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Catacomb Dragon");
        Permanent dragonPermanent = findPermanent(player1, "Catacomb Dragon");
        assertThat(gqs.hasKeyword(gd, dragonPermanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The found Dragon is exiled at the beginning of the next end step")
    void foundDragonExiledAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        setUpZirilan();
        CatacombDragon dragon = new CatacombDragon();
        harness.setLibrary(player1, List.of(dragon, new JungleWurm()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Catacomb Dragon");

        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Catacomb Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(dragon);
    }

    @Test
    @DisplayName("A Dragon found during an end step is exiled at the following end step")
    void foundDragonDuringEndStepExiledAtFollowingEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        setUpZirilan();
        CatacombDragon dragon = new CatacombDragon();
        harness.setLibrary(player1, List.of(dragon));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Catacomb Dragon");
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Catacomb Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(dragon);
    }

    @Test
    @DisplayName("The controller may decline to find a matching Dragon")
    void mayDeclineToFindDragon() {
        setUpZirilan();
        CatacombDragon dragon = new CatacombDragon();
        JungleWurm wurm = new JungleWurm();
        harness.setLibrary(player1, List.of(dragon, wurm));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Catacomb Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(dragon, wurm);
    }

    @Test
    @DisplayName("Finding no Dragon leaves the battlefield unchanged")
    void noDragonFound() {
        setUpZirilan();
        JungleWurm wurm = new JungleWurm();
        harness.setLibrary(player1, List.of(wurm));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Jungle Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wurm);
    }
}
