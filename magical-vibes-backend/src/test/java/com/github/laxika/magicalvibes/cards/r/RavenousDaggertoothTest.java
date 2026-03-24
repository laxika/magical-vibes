package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RavenousDaggertoothTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ravenous Daggertooth has one ON_DEALT_DAMAGE effect")
    void hasCorrectEffect() {
        RavenousDaggertooth card = new RavenousDaggertooth();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect effect = (GainLifeEffect) card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, controller gains 2 life")
    void spellDamageGainsLife() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Daggertooth (non-lethal for 3/2... actually lethal)

        // Shock deals 2 damage to a 3/2, which is lethal. The creature dies but the enrage trigger
        // still fires because it was dealt damage before dying.
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve the enrage trigger

        // Controller should have gained 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("When dealt non-lethal combat damage, controller gains 2 life")
    void combatDamageGainsLife() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent daggertooth = gd.playerBattlefields.get(player2.getId()).getFirst();
        daggertooth.setSummoningSick(false);
        daggertooth.setBlocking(true);
        daggertooth.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // enrage trigger on stack
        harness.passBothPriorities(); // resolve trigger

        // Controller should have gained 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);

        // Daggertooth survives (3/2 takes 1 damage from 1/1)
        harness.assertOnBattlefield(player2, "Ravenous Daggertooth");
    }

    @Test
    @DisplayName("Opponent does not gain life when Daggertooth is dealt damage")
    void opponentDoesNotGainLife() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve enrage trigger

        // Opponent (player1) should NOT have gained any life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Enrage triggers each time damage is dealt")
    void triggersMultipleTimes() {
        harness.addToBattlefield(player2, new RavenousDaggertooth());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID daggertoothId = harness.getPermanentId(player2, "Ravenous Daggertooth");

        // First Shock — kills the 3/2
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve enrage trigger

        // Controller gained 2 life from the first trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
    }
}
