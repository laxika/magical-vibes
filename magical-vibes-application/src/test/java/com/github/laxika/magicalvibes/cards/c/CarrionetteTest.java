package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrionetteTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay exiles both the target creature and Carrionette")
    void declineExilesBoth() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName())
                .contains("Grizzly Bears", "Carrionette");
    }

    @Test
    @DisplayName("Paying {2} keeps the creature on the battlefield and Carrionette in the graveyard")
    void payingStopsBothExiles() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        harness.assertInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Accepting without the mana still exiles both")
    void acceptWithoutManaExilesBoth() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Carrionette");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves; Carrionette stays in the graveyard")
    void fizzlesWhenTargetGone() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Carrionette");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Ability requires a creature target")
    void requiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new Carrionette()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
