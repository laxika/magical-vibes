package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.n.NovaCleric;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntingDefender.class, ElvishWarrior.class, NovaCleric.class, Shock.class, WoodlandChangeling.class})
class DauntingDefenderTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 noncombat damage to itself, a Cleric you control")
    void preventsOneDamageFromNoncombatSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage from each source")
    void preventsOneDamageFromEachSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player2, defender.getId());
        castShock(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents 1 damage to another Cleric you control")
    void preventsDamageToAnotherClericYouControl() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new NovaCleric());

        castShock(player2, cleric.getId());

        assertThat(cleric.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doesn't prevent damage to non-Clerics or players")
    void doesNotPreventDamageToNonClericsOrPlayers() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        int lifeBefore = gd.getLife(player1.getId());

        castShock(player2, elf.getId());
        castShock(player2, player1.getId());

        assertThat(elf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Doesn't prevent damage to an opponent's Cleric")
    void doesNotPreventDamageToAnOpponentsCleric() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new NovaCleric());

        castShock(player1, opponentCleric.getId());

        assertThat(opponentCleric.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCleric);
    }

    @Test
    @DisplayName("Prevents 1 combat damage to a Cleric you control")
    void preventsOneCombatDamageFromEachSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        defender.setSummoningSick(false);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Daunting Defender prevents 1 damage")
    void multipleDefendersEachPreventOneDamage() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new NovaCleric());

        castShock(player2, defender.getId());
        castShock(player2, cleric.getId());

        assertThat(defender.getMarkedDamage()).isZero();
        assertThat(cleric.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(defender, cleric);
    }

    @Test
    @DisplayName("Also prevents damage from your own source")
    void alsoPreventsDamageFromYourOwnSource() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player1, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stops protecting Clerics after Daunting Defender dies")
    void stopsProtectingClericsAfterDefenderDies() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new NovaCleric());

        castShock(player2, defender.getId());
        castShock(player2, defender.getId());
        castShock(player2, defender.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(defender);

        castShock(player2, cleric.getId());

        assertThat(cleric.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cleric);
    }

    @Test
    @DisplayName("Prevents 1 damage to a changeling you control, which is every creature type")
    void preventsDamageToChangelingYouControl() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        castShock(player2, changeling.getId());

        assertThat(changeling.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage to a creature an effect made a Cleric")
    void preventsDamageToCreatureMadeClericByEffect() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        PermanentGrants.subtype(gd, elf, CardSubtype.CLERIC);

        castShock(player2, elf.getId());

        assertThat(elf.getMarkedDamage()).isEqualTo(1);
    }

    private void castShock(Player player, UUID targetId) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
