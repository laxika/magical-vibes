package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KashiTribeReaver.class, MossKami.class})
class KashiTribeReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
        // 5/5 survives the Reaver's 3 damage, so the tap/untap lock is observable.
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat-damage tap and untap lock resolve as one triggered ability")
    void combatDamageTriggerResolvesBothInstructionsTogether() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        mossKami.setBlocking(true);
        mossKami.addBlockingTarget(0);
        mossKami.addBlockingTargetId(reaver.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        assertThat(mossKami.isTapped()).isFalse();
        assertThat(mossKami.getSkipUntapCount()).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock any creature")
    void unblockedDamageDoesNotTapCreatures() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isFalse();
        assertThat(mossKami.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Activating {1}{G} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reaver.getRegenerationShield()).isEqualTo(1);
    }
    @Test
    @DisplayName("The damaged creature skips only its controller's next untap")
    void lockExpiresAfterOneControllerUntap() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        reaver.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(mossKami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration saves the Reaver from lethal combat damage without stopping its trigger")
    void regenerationSavesReaverInCombat() {
        Permanent reaver = addCreatureReady(player1, new KashiTribeReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(reaver.isTapped()).isFalse();

        reaver.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reaver);
        assertThat(reaver.isTapped()).isTrue();
        assertThat(reaver.getRegenerationShield()).isZero();
        assertThat(reaver.getMarkedDamage()).isZero();
        assertThat(reaver.isAttacking()).isFalse();
        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);
    }

}
