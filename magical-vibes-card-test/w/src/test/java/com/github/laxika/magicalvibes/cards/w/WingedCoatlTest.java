package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingedCoatl.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class WingedCoatlTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Winged Coatl puts it onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new WingedCoatl(), "{1}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Winged Coatl");
    }

    @Test
    @DisplayName("Can cast during the combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new WingedCoatl(), "{1}{G}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Winged Coatl");
    }

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(gd, player2);
        harness.castFromHand(player1, new WingedCoatl(), "{1}{G}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Winged Coatl");
    }

    @Test
    @DisplayName("Winged Coatl cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByGroundCreature() {
        Permanent coatl = addCreatureReady(player1, new WingedCoatl());
        coatl.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Winged Coatl can be blocked by a flying creature")
    void canBeBlockedByFlyer() {
        Permanent coatl = addCreatureReady(player1, new WingedCoatl());
        coatl.setAttacking(true);

        Permanent flyingBlocker = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Attacking Winged Coatl destroys a larger flying blocker with deathtouch")
    void deathtouchDestroysLargerFlyingBlocker() {
        Permanent coatl = addCreatureReady(player1, new WingedCoatl());
        coatl.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Winged Coatl");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Winged Coatl deals one damage to a player without a deathtouch loss")
    void deathtouchDoesNotDestroyPlayer() {
        Permanent coatl = addCreatureReady(player1, new WingedCoatl());
        coatl.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Winged Coatl");
    }

    @Test
    @DisplayName("Winged Coatl's 1 deathtouch damage destroys a larger blocked creature")
    void deathtouchDestroysLargerCreature() {
        // Hill Giant (3/3) attacks; Winged Coatl (1/1 deathtouch, flying) blocks it.
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        hillGiant.setAttacking(true);

        Permanent coatl = addCreatureReady(player2, new WingedCoatl());
        coatl.setBlocking(true);
        coatl.addBlockingTarget(0);

        resolveCombat();

        // 1 deathtouch damage destroys the 3/3.
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        // Winged Coatl (1 toughness) dies to the 3 damage it took back.
        harness.assertInGraveyard(player2, "Winged Coatl");
    }
}
