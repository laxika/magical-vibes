package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoobyTrap.class, CavesOfKoilos.class, Forest.class, GrizzlyBears.class})
class BoobyTrapTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2; // avoid first-turn draw skip
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private Permanent addTrap(Player controller, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(controller, new BoobyTrap());
        perm.setChosenName(chosenName);
        return perm;
    }

    @Test
    @DisplayName("Resolving Booby Trap awaits a card name choice and stamps it on the permanent")
    void resolvingChoosesCardName() {
        harness.castFromHand(player1, new BoobyTrap(), "{6}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent perm = findPermanent(player1, "Booby Trap");
        assertThat(perm.getChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Booby Trap allows naming a nonbasic land but not a basic land")
    void allowsNonbasicLandName() {
        harness.setHand(player2, List.of(new CavesOfKoilos(), new Forest()));
        harness.castFromHand(player1, new BoobyTrap(), "{6}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice =
                (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).contains("Caves of Koilos");
        assertThat(choice.options()).doesNotContain("Forest");

        harness.handleListChoice(player1, "Caves of Koilos");

        Permanent perm = findPermanent(player1, "Booby Trap");
        assertThat(perm.getChosenName()).isEqualTo("Caves of Koilos");
    }

    @Test
    @DisplayName("Chosen player drawing the named card sacrifices the trap and takes 10 damage")
    void namedDrawDealsTenAndSacrifices() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Booby Trap trigger

        harness.assertLife(player2, 10);
        harness.assertNotOnBattlefield(player1, "Booby Trap");
        harness.assertInGraveyard(player1, "Booby Trap");
    }

    @Test
    @DisplayName("Multiple matching draws reveal each card but only the first trigger deals damage")
    void multipleMatchingDrawsOnlyDealDamageOnce() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() ->
                harness.getDrawService().resolveDrawCards(gd, player2.getId(), 2));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("reveals") && log.contains("Booby Trap"))
                .count()).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertNotOnBattlefield(player1, "Booby Trap");
    }

    @Test
    @DisplayName("Chosen player drawing a different card does nothing")
    void differentDrawDoesNothing() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        advanceToDraw(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Booby Trap");
    }

    @Test
    @DisplayName("Booby Trap does not trigger when its controller draws the named card")
    void controllerDrawDoesNotTrigger() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Booby Trap");
    }

    @Test
    @DisplayName("The chosen player reveals each card they draw")
    void chosenPlayerRevealsDraws() {
        addTrap(player1, "Grizzly Bears");
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        advanceToDraw(player2);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("reveals") && log.contains("Forest") && log.contains("Booby Trap"));
    }
}
