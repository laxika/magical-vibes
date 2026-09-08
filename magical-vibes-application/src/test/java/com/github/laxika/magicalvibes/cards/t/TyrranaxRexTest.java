package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TyrranaxRexTest extends BaseCardTest {

    @Test
    @DisplayName("Tyrranax Rex cannot be countered by Cancel")
    void cannotBeCountered() {
        TyrranaxRex rex = new TyrranaxRex();
        harness.setHand(player1, List.of(rex));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rex.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tyrranax Rex");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Ward {4} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent rex = addRexReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, rex.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Tyrranax Rex");
    }

    @Test
    @DisplayName("Paying Ward {4} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent rex = addRexReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, rex.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(rex.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Toxic 4 gives the defending player four poison counters")
    void toxicDealsFourPoisonCounters() {
        harness.setLife(player2, 20);
        Permanent rex = addRexReady(player1);
        rex.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Haste allows Tyrranax Rex to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        Permanent rex = harness.addToBattlefieldAndReturn(player1, new TyrranaxRex());

        declareAttackers(List.of(0));

        assertThat(rex.isTapped()).isTrue();
    }

    private Permanent addRexReady(Player player) {
        return addCreatureReady(player, new TyrranaxRex());
    }
}
