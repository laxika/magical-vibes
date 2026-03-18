package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodcrazedNeonate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDamageDealerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakishHeirTest extends BaseCardTest {

    private Permanent addReadyRakishHeir() {
        Permanent perm = new Permanent(new RakishHeir());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyVampire() {
        Permanent perm = new Permanent(new BloodcrazedNeonate());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyNonVampire() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has PutCountersOnDamageDealerEffect with Vampire predicate")
    void hasCorrectEffects() {
        RakishHeir card = new RakishHeir();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(PutCountersOnDamageDealerEffect.class);

        PutCountersOnDamageDealerEffect effect =
                (PutCountersOnDamageDealerEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(effect.powerModifier()).isEqualTo(1);
        assertThat(effect.toughnessModifier()).isEqualTo(1);
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.predicate()).isInstanceOf(PermanentHasSubtypePredicate.class);
        assertThat(((PermanentHasSubtypePredicate) effect.predicate()).subtype()).isEqualTo(CardSubtype.VAMPIRE);
    }

    // ===== Trigger: Vampire deals combat damage to a player =====

    @Test
    @DisplayName("Another attacking Vampire gets a +1/+1 counter when dealing combat damage")
    void vampireGetsCounterOnCombatDamage() {
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage from Bloodcrazed Neonate
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve both triggered abilities (Neonate's own + Rakish Heir's)
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Vampire should have 2 +1/+1 counters (1 from own ability + 1 from Rakish Heir)
        assertThat(vampire.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Vampire creature does not trigger Rakish Heir")
    void nonVampireDoesNotTrigger() {
        addReadyRakishHeir();
        Permanent bears = addReadyNonVampire();
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage from Grizzly Bears
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // No triggered abilities to resolve — bears have no combat damage trigger, and Rakish Heir
        // doesn't trigger for non-Vampires
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Rakish Heir triggers for itself when it deals combat damage")
    void triggersForItself() {
        Permanent heir = addReadyRakishHeir();
        heir.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve Rakish Heir's triggered ability
        harness.passBothPriorities();

        // Rakish Heir is a Vampire, so it should get a counter from its own trigger
        assertThat(heir.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Rakish Heirs each trigger separately")
    void multipleRakishHeirs() {
        addReadyRakishHeir();
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Resolve all triggered abilities (Neonate's own + 2x Rakish Heir)
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Vampire should have 3 +1/+1 counters (1 from own ability + 2 from Rakish Heirs)
        assertThat(vampire.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("No counter when Vampire is blocked and killed before damage")
    void noCounterWhenVampireBlockedAndKilled() {
        addReadyRakishHeir();
        Permanent vampire = addReadyVampire();
        vampire.setAttacking(true);

        // 4/4 blocker kills the 2/1 Neonate (index 1 on attacker's battlefield)
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Vampire should be dead — no combat damage to player means no trigger
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bloodcrazed Neonate"));
    }
}
