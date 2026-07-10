package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoobyTrapTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private Permanent addTrap(Player controller, String chosenName) {
        Permanent perm = new Permanent(new BoobyTrap());
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    // ===== ETB card name choice =====

    @Test
    @DisplayName("Resolving Booby Trap awaits a card name choice and stamps it on the permanent")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new BoobyTrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Booby Trap"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Grizzly Bears");
    }

    // ===== Draw trigger =====

    @Test
    @DisplayName("Chosen player drawing the named card sacrifices the trap and takes 10 damage")
    void namedDrawDealsTenAndSacrifices() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Booby Trap trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Booby Trap"));
        harness.assertInGraveyard(player1, "Booby Trap");
    }

    @Test
    @DisplayName("Chosen player drawing a different card does nothing")
    void differentDrawDoesNothing() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Booby Trap"));
    }

    @Test
    @DisplayName("Booby Trap does not trigger when its controller draws the named card")
    void controllerDrawDoesNotTrigger() {
        addTrap(player1, "Grizzly Bears");
        harness.setLife(player1, 20);
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Booby Trap"));
    }

    @Test
    @DisplayName("The chosen player reveals each card they draw")
    void chosenPlayerRevealsDraws() {
        addTrap(player1, "Grizzly Bears");
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        advanceToDraw(player2);

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("reveals") && log.contains("Forest") && log.contains("Booby Trap"));
    }
}
