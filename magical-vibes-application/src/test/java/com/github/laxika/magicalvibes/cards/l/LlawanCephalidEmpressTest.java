package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlawanCephalidEmpress.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class, VaporSnag.class})
class LlawanCephalidEmpressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning opponents' blue creatures and leaves other creatures alone")
    void entersAndReturnsOpponentsBlueCreatures() {
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LlawanCephalidEmpress()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llawan, Cephalid Empress");
        harness.assertOnBattlefield(player1, "Merfolk of the Pearl Trident");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Merfolk of the Pearl Trident");
        harness.assertInHand(player2, "Merfolk of the Pearl Trident");
    }

    @Test
    @DisplayName("Opponents cannot cast blue creature spells")
    void opponentCannotCastBlueCreatureSpell() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        harness.setHand(player2, List.of(new MerfolkOfThePearlTrident()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        prepareOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction leaves other opponent spells castable")
    void restrictionIsScopedToOpponentsBlueCreatures() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        prepareOpponentTurn();
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new VaporSnag()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        prepareOpponentTurn();
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Llawan's controller can cast blue creature spells")
    void controllerCanCastBlueCreatureSpell() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        harness.setHand(player1, List.of(new MerfolkOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
