package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CatacombDragon;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZirilanOfTheClaw.class, CatacombDragon.class, JungleWurm.class, RayOfCommand.class})
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
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

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

    @Test
    @DisplayName("An empty library does not leave an unresolved search")
    void emptyLibrary() {
        setUpZirilan();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The search does not find Dragons in the opponent's library")
    void searchesOnlyControllersLibrary() {
        setUpZirilan();
        JungleWurm wurm = new JungleWurm();
        CatacombDragon opposingDragon = new CatacombDragon();
        harness.setLibrary(player1, List.of(wurm));
        harness.setLibrary(player2, List.of(opposingDragon));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Catacomb Dragon");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingDragon);
    }

    @Test
    @DisplayName("Haste expires during cleanup even when exile waits for the following end step")
    void hasteExpiresBeforeFollowingEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        setUpZirilan();
        harness.setLibrary(player1, List.of(new CatacombDragon()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        Permanent dragon = findPermanent(player1, "Catacomb Dragon");
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Catacomb Dragon");
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HASTE)).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The delayed exile retains Zirilan as its source and the resolving ability's controller")
    void delayedExileRetainsSourceAndControllerAfterDragonIsStolen() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        setUpZirilan();
        Permanent zirilan = findPermanent(player1, "Zirilan of the Claw");
        harness.setLibrary(player1, List.of(new CatacombDragon()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        Permanent dragon = findPermanent(player1, "Catacomb Dragon");

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        harness.assertOnBattlefield(player2, "Catacomb Dragon");

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        StackEntry exileTrigger = gd.stack.getLast();
        assertThat(exileTrigger.getControllerId()).isEqualTo(player1.getId());
        assertThat(exileTrigger.getSourcePermanentId()).isEqualTo(zirilan.getId());
        assertThat(exileTrigger.getCard()).isSameAs(zirilan.getCard());
    }
}
