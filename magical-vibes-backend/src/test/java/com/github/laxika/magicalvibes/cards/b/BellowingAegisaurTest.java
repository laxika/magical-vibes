package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BellowingAegisaurTest extends BaseCardTest {

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Permanent not found: " + cardName));
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Bellowing Aegisaur has one ON_DEALT_DAMAGE effect")
    void hasCorrectEffect() {
        BellowingAegisaur card = new BellowingAegisaur();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0))
                .isInstanceOf(PutPlusOnePlusOneCounterOnEachControlledPermanentEffect.class);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, puts +1/+1 counter on each other own creature")
    void spellDamagePutsCountersOnOtherCreatures() {
        harness.addToBattlefield(player2, new BellowingAegisaur());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID aegisaurId = harness.getPermanentId(player2, "Bellowing Aegisaur");
        harness.castInstant(player1, 0, aegisaurId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Aegisaur (non-lethal for 3/5)

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Aegisaur should survive (3/5 takes 2 damage)
        harness.assertOnBattlefield(player2, "Bellowing Aegisaur");

        // Aegisaur itself should NOT have a +1/+1 counter (says "each other")
        Permanent aegisaur = findPermanent(player2, "Bellowing Aegisaur");
        assertThat(aegisaur.getPlusOnePlusOneCounters()).isZero();

        // Other creatures should each have 1 +1/+1 counter
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);

        Permanent wizard = findPermanent(player2, "Fugitive Wizard");
        assertThat(wizard.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put counters on opponent's creatures")
    void doesNotPutCountersOnOpponentCreatures() {
        harness.addToBattlefield(player2, new BellowingAegisaur());
        harness.addToBattlefield(player1, new GrizzlyBears()); // opponent's creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID aegisaurId = harness.getPermanentId(player2, "Bellowing Aegisaur");
        harness.castInstant(player1, 0, aegisaurId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve trigger

        // Opponent's creature should NOT get a counter
        Permanent opponentBears = findPermanent(player1, "Grizzly Bears");
        assertThat(opponentBears.getPlusOnePlusOneCounters()).isZero();
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal combat damage, puts +1/+1 counters on other own creatures")
    void combatDamagePutsCountersOnOtherCreatures() {
        harness.addToBattlefield(player2, new BellowingAegisaur());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent aegisaur = gd.playerBattlefields.get(player2.getId()).get(0);
        aegisaur.setSummoningSick(false);
        aegisaur.setBlocking(true);
        aegisaur.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and trigger
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // trigger on stack
        harness.passBothPriorities(); // resolve trigger

        // Aegisaur should survive (3/5 takes 1 damage from 1/1)
        harness.assertOnBattlefield(player2, "Bellowing Aegisaur");

        // Grizzly Bears should have a +1/+1 counter
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Aegisaur should NOT have a +1/+1 counter
        Permanent aegisaurAfter = findPermanent(player2, "Bellowing Aegisaur");
        assertThat(aegisaurAfter.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("No other own creatures means no counters are placed")
    void noOtherCreaturesMeansNoCounters() {
        harness.addToBattlefield(player2, new BellowingAegisaur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID aegisaurId = harness.getPermanentId(player2, "Bellowing Aegisaur");
        harness.castInstant(player1, 0, aegisaurId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve trigger

        // Aegisaur survives with no counter on itself
        harness.assertOnBattlefield(player2, "Bellowing Aegisaur");
        Permanent aegisaur = findPermanent(player2, "Bellowing Aegisaur");
        assertThat(aegisaur.getPlusOnePlusOneCounters()).isZero();
    }
}
