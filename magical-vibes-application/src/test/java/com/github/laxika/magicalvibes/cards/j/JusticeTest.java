package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JusticeTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("A red spell dealing damage reflects that much to the spell's controller")
    void redSpellDamageReflectedToController() {
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Shock resolves: player2 takes 2, Justice trigger queued
        harness.passBothPriorities(); // Justice resolves: 2 to Shock's controller (player1)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A red creature's combat damage reflects that much to its controller")
    void redCreatureCombatDamageReflectedToController() {
        harness.addToBattlefield(player1, new HillGiant()); // red 3/3
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage: player2 takes 3, Justice trigger queued
        harness.passBothPriorities(); // Justice resolves: 3 to attacker's controller (player1)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-red source dealing damage does not trigger Justice")
    void nonRedSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // green 2/2
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage: player2 takes 2, no Justice trigger
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A red spell damaging several creatures reflects the summed total once")
    void massDamageReflectsSummedTotal() {
        harness.addToBattlefield(player1, new GiantSpider()); // 2/4
        harness.addToBattlefield(player1, new GiantSpider()); // 2/4
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Pyroclasm deals 2 to each of 3 creatures = 6 total; Justice queued
        harness.passBothPriorities(); // Justice resolves: 6 to Pyroclasm's controller (player1)

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Paying {W}{W} at upkeep keeps Justice on the battlefield")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new Justice());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Justice");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Justice")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new Justice());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Justice");
    }
}
