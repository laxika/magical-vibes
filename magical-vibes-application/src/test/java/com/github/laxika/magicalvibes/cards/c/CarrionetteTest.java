package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carrionette.class, CursedScroll.class, HornedTurtle.class})
class CarrionetteTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay exiles both the target creature and Carrionette")
    void declineExilesBoth() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, turtle.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Horned Turtle");
        harness.assertNotInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName())
                .contains("Horned Turtle", "Carrionette");
    }

    @Test
    @DisplayName("Paying {2} keeps the creature on the battlefield and Carrionette in the graveyard")
    void payingStopsBothExiles() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, turtle.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(turtle);
        harness.assertInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Accepting without the mana still exiles both")
    void acceptWithoutManaExilesBoth() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, turtle.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Horned Turtle");
        harness.assertNotInGraveyard(player1, "Carrionette");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves; Carrionette stays in the graveyard")
    void fizzlesWhenTargetGone() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, turtle.getId());
        gd.playerBattlefields.get(player2.getId()).remove(turtle);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Can be activated during an upkeep")
    void canBeActivatedDuringUpkeep() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, turtle.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Horned Turtle");
        harness.assertNotInGraveyard(player1, "Carrionette");
    }

    @Test
    @DisplayName("Ability requires a creature target")
    void requiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CursedScroll());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
