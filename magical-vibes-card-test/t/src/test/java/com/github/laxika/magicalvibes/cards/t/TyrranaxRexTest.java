package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.b.BiliousSkulldweller;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrranaxRex.class, Cancel.class, Shock.class, BiliousSkulldweller.class})
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

    @Test
    @DisplayName("Toxic gives four poison counters immediately without using the stack")
    void toxicIsImmediateCombatDamageResult() {
        Permanent rex = addRexReady(player1);
        rex.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 12);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample deals excess damage and toxic gives four poison even when Rex dies")
    void trampleDamageGivesPoisonWhenRexDies() {
        Permanent rex = addRexReady(player1);
        rex.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BiliousSkulldweller());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                java.util.Map.of(blocker.getId(), 1, player2.getId(), 7));
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Tyrranax Rex");
        harness.assertInGraveyard(player2, "Bilious Skulldweller");
    }

    @Test
    @DisplayName("Ward does not trigger for the controller's own spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent rex = addRexReady(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, rex.getId());

        assertThat(rex.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addRexReady(Player player) {
        return addCreatureReady(player, new TyrranaxRex());
    }
}
