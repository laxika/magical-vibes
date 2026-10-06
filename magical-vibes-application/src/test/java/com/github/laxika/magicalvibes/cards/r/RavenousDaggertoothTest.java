package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousDaggertooth.class, DualShot.class, JungleDelver.class, LightningStrike.class})
class RavenousDaggertoothTest extends BaseCardTest {

    @Test
    @DisplayName("Lethal spell damage still triggers a gain of exactly 2 life")
    void spellDamageGainsLife() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ravenous Daggertooth");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When dealt non-lethal combat damage, controller gains 2 life")
    void combatDamageGainsLife() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        daggertooth.setBlocking(true);
        daggertooth.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, lifeBefore);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Ravenous Daggertooth");
    }

    @Test
    @DisplayName("Opponent does not gain life when Daggertooth is dealt damage")
    void opponentDoesNotGainLife() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        int opponentLifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, opponentLifeBefore);
    }

    @Test
    @DisplayName("Separate damage events each trigger, including the event that kills Daggertooth")
    void triggersMultipleTimes() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new DualShot(), new DualShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");
        harness.castInstant(player1, 0, List.of(daggertoothId));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Ravenous Daggertooth");

        harness.castInstant(player1, 0, List.of(daggertoothId));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Ravenous Daggertooth");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore + 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous combat damage from two blockers triggers only once")
    void simultaneousCombatDamageTriggersOnce() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player1, new RavenousDaggertooth());
        daggertooth.setSummoningSick(false);
        daggertooth.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1, secondBlocker.getId(), 2));

        harness.assertInGraveyard(player1, "Ravenous Daggertooth");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to another creature does not trigger Daggertooth")
    void damageToAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, List.of(otherCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jungle Delver");
        harness.assertOnBattlefield(player2, "Ravenous Daggertooth");
        harness.assertLife(player2, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
