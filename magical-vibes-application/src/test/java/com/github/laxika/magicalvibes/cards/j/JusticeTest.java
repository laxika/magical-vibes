package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FolkOfThePines;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Justice.class, BalduvianBarbarians.class, BalduvianBears.class, Incinerate.class,
        FolkOfThePines.class, Pyroclasm.class, MoorFiend.class})
class JusticeTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell dealing damage reflects that much to the spell's controller")
    void redSpellDamageReflectedToController() {
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Incinerate resolves: player2 takes 3, Justice trigger queued
        harness.passBothPriorities(); // Justice resolves: 3 to Incinerate's controller (player1)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A red creature's combat damage reflects that much to its controller")
    void redCreatureCombatDamageReflectedToController() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities(); // combat damage: player2 takes 3, Justice trigger queued
        harness.passBothPriorities(); // Justice resolves: 3 to attacker's controller (player1)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A red creature dealing damage to a blocker reflects that much to its controller")
    void redCreatureDamageToBlockerReflectedToController() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attacker.setAttacking(true);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities(); // Justice resolves: 3 to the attacker's controller

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-red source dealing damage does not trigger Justice")
    void nonRedSourceDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A red spell damaging several creatures reflects the summed total once")
    void massDamageReflectsSummedTotal() {
        harness.addToBattlefield(player1, new FolkOfThePines()); // 2/5
        harness.addToBattlefield(player1, new FolkOfThePines()); // 2/5
        harness.addToBattlefield(player2, new FolkOfThePines()); // 2/5
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
    void deadRedCreatureStillTriggersJustice() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        Permanent blocker = addCreatureReady(player2, new MoorFiend());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
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
