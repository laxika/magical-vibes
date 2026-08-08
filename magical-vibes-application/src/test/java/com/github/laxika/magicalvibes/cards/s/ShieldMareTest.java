package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShieldMareTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters the battlefield")
    void gainsLifeOnEnter() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new ShieldMare()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature
        harness.passBothPriorities(); // resolve the enter trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains 3 life when an opponent's spell targets it")
    void gainsLifeOnOpponentSpell() {
        harness.addToBattlefield(player1, new ShieldMare());
        UUID mareId = harness.getPermanentId(player1, "Shield Mare");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, mareId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains 3 life when an opponent's activated ability targets it")
    void gainsLifeOnOpponentAbility() {
        harness.addToBattlefield(player1, new ShieldMare());
        UUID mareId = harness.getPermanentId(player1, "Shield Mare");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, mareId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Does not trigger when its controller's own spell targets it")
    void noLifeOnOwnSpell() {
        harness.addToBattlefield(player1, new ShieldMare());
        UUID mareId = harness.getPermanentId(player1, "Shield Mare");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mareId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Cannot be blocked by a red creature")
    void cannotBeBlockedByRed() {
        Permanent mare = new Permanent(new ShieldMare());
        mare.setSummoningSick(false);
        mare.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mare);

        Permanent giant = new Permanent(new HillGiant()); // red
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a non-red creature")
    void canBeBlockedByNonRed() {
        Permanent mare = new Permanent(new ShieldMare());
        mare.setSummoningSick(false);
        mare.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mare);

        Permanent bears = new Permanent(new GrizzlyBears()); // green
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }
}
