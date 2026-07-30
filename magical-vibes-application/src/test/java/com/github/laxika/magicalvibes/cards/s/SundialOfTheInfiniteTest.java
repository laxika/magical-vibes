package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SundialOfTheInfiniteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ends the turn and passes to the next player")
    void endsTheTurn() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID activePlayerBefore = gd.activePlayerId;
        int turnBefore = gd.turnNumber;

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isNotEqualTo(activePlayerBefore);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ending the turn exiles other spells from the stack")
    void exilesSpellsOnStack() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ending the turn resets until-end-of-turn modifiers")
    void resetsEndOfTurnModifiers() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        bears.setPowerModifier(3);
        bears.setToughnessModifier(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Taps the Sundial and spends {1} as the cost")
    void tapsSundialAsCost() {
        Permanent sundial = addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sundial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadySundial(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addReadySundial(Player player) {
        Permanent perm = new Permanent(new SundialOfTheInfinite());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
