package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.b.BoggartRamGang;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.p.PowerOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InquisitorsSnare.class, AshenmoorCohort.class, BoggartRamGang.class,
        BriarberryCohort.class, PowerOfFire.class})
class InquisitorsSnareTest extends BaseCardTest {

    // ===== Prevention =====

    @Test
    @DisplayName("Target attacking creature is prevented from dealing damage")
    void preventsDamage() {
        Permanent attacker = addAttacker(player2, new BriarberryCohort());
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Prevented attacker deals no combat damage to the player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new BriarberryCohort());
        castSnare(attacker);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    // ===== Conditional destroy =====

    @Test
    @DisplayName("Black or red creature is also destroyed")
    void destroysRedCreature() {
        Permanent attacker = addAttacker(player2, new BoggartRamGang()); // red/green 3/3
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Boggart Ram-Gang");
    }

    @Test
    @DisplayName("Black creature is also destroyed")
    void destroysBlackCreature() {
        Permanent attacker = addAttacker(player2, new AshenmoorCohort());
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Ashenmoor Cohort");
    }

    @Test
    @DisplayName("Non-black-non-red creature is prevented but not destroyed")
    void doesNotDestroyNonBlackNonRedCreature() {
        Permanent attacker = addAttacker(player2, new BriarberryCohort()); // blue
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.assertNotInGraveyard(player2, "Briarberry Cohort");
    }

    @Test
    @DisplayName("Target blocking creature is prevented from dealing damage")
    void preventsDamageFromBlockingCreature() {
        Permanent attacker = addAttacker(player2, new BoggartRamGang());
        Permanent blocker = addCreatureReady(player1, new BriarberryCohort());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castSnare(blocker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(blocker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Noncombat damage from the target creature is also prevented")
    void preventsNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent source = addAttacker(player2, new BriarberryCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PowerOfFire());
        aura.setAttachedTo(source.getId());

        castSnare(source);

        int sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(source);
        harness.activateAbility(player2, sourceIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(source.isTapped()).isTrue();
    }

    // ===== Prevention wears off =====

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent attacker = addAttacker(player2, new BriarberryCohort());
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new BriarberryCohort());
        harness.setHand(player1, List.of(new InquisitorsSnare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Briarberry Cohort");

        assertThatThrownBy(() -> harness.getGameService()
                .playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castSnare(Permanent target) {
        harness.setHand(player1, List.of(new InquisitorsSnare()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
