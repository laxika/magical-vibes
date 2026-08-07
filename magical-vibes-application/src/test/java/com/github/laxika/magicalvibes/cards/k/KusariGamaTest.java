package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KusariGamaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can pay {2} for +1/+0 until end of turn")
    void grantedPumpAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addKusariGama(player1).setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to a blocker also hits each other creature the defending player controls")
    void damagesOtherDefendingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addKusariGama(player1).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        blockAttacker(player2, new GrizzlyBears(), 0);
        addCreatureReady(player2, new KeenEyedArchers());

        resolveCombatDamage();
        harness.passBothPriorities();

        // The 2/2 bystander took the same 2 damage the blocker was dealt.
        harness.assertInGraveyard(player2, "Keen-Eyed Archers");
    }

    @Test
    @DisplayName("The blocking creature that was damaged is not dealt the extra damage again")
    void blockingCreatureExcluded() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addKusariGama(player1).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // A 4/4 blocker survives the 2 combat damage; a second hit of 2 would kill it.
        blockAttacker(player2, new SerraAngel(), 0);
        addCreatureReady(player2, new KeenEyedArchers());

        resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Keen-Eyed Archers");
    }

    @Test
    @DisplayName("No trigger when the damaged creature is not blocking")
    void noTriggerWhenDamagedCreatureIsNotBlocking() {
        Permanent blocker = addCreatureReady(player1, new SerraAngel());
        addKusariGama(player1).setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KeenEyedArchers());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The attacker it damaged was not a blocking creature, so nothing else was hit.
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Kusari-Gama"));
        harness.assertOnBattlefield(player2, "Keen-Eyed Archers");
    }

    @Test
    @DisplayName("No trigger while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addKusariGama(player1);
        attacker.setAttacking(true);

        blockAttacker(player2, new GrizzlyBears(), 0);
        addCreatureReady(player2, new KeenEyedArchers());

        resolveCombatDamage();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Kusari-Gama"));
        harness.assertOnBattlefield(player2, "Keen-Eyed Archers");
    }

    private Permanent addKusariGama(Player player) {
        Permanent perm = new Permanent(new KusariGama());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = new Permanent(blockerCard);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(blocker.getId()).add(perm);
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
