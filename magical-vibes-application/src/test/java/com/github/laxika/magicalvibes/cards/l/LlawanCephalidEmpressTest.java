package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.c.ChurningEddy;
import com.github.laxika.magicalvibes.cards.h.HydromorphGull;
import com.github.laxika.magicalvibes.cards.o.ObsessiveSearch;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlawanCephalidEmpress.class, HydromorphGull.class, BaskingRootwalla.class,
        ObsessiveSearch.class, ChurningEddy.class, TaintedIsle.class})
class LlawanCephalidEmpressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning opponents' blue creatures and leaves other creatures alone")
    void entersAndReturnsOpponentsBlueCreatures() {
        harness.addToBattlefield(player1, new HydromorphGull());
        harness.addToBattlefield(player2, new HydromorphGull());
        harness.addToBattlefield(player2, new BaskingRootwalla());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new LlawanCephalidEmpress(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llawan, Cephalid Empress");
        harness.assertOnBattlefield(player1, "Hydromorph Gull");
        harness.assertOnBattlefield(player2, "Basking Rootwalla");
        harness.assertNotOnBattlefield(player2, "Hydromorph Gull");
        harness.assertInHand(player2, "Hydromorph Gull");
    }

    @Test
    @DisplayName("Opponents cannot cast blue creature spells")
    void opponentCannotCastBlueCreatureSpell() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        prepareOpponentTurn();

        assertThatThrownBy(() -> harness.castFromHand(player2, new HydromorphGull(), "{3}{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction leaves other opponent spells castable")
    void restrictionIsScopedToOpponentsBlueCreatures() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        prepareOpponentTurn();
        harness.castFromHand(player2, new BaskingRootwalla(), "{G}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        prepareOpponentTurn();
        harness.castFromHand(player2, new ObsessiveSearch(), "{U}");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Llawan's controller can cast blue creature spells")
    void controllerCanCastBlueCreatureSpell() {
        harness.addToBattlefield(player1, new LlawanCephalidEmpress());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new HydromorphGull(), "{3}{U}{U}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The casting restriction ends when Llawan leaves the battlefield")
    void restrictionEndsWhenLlawanLeavesBattlefield() {
        Permanent llawan = harness.addToBattlefieldAndReturn(player1, new LlawanCephalidEmpress());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TaintedIsle());

        harness.setHand(player2, List.of(new ChurningEddy()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareOpponentTurn();
        harness.castSorcery(player2, 0, List.of(llawan.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llawan, Cephalid Empress");
        harness.assertInHand(player1, "Llawan, Cephalid Empress");
        harness.assertNotOnBattlefield(player1, "Tainted Isle");
        harness.assertInHand(player1, "Tainted Isle");

        prepareOpponentTurn();
        harness.castFromHand(player2, new HydromorphGull(), "{3}{U}{U}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
