package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.t.TakenosCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NekoTe.class, TakenosCavalry.class, GnarledMass.class})
class NekoTeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature damaging a creature taps it and keeps it tapped")
    void equippedCreatureDamagingCreatureTapsAndLocksIt() {
        Permanent cavalry = addCreatureReady(player1, new TakenosCavalry());
        Permanent nekoTe = attachNekoTe(player1, cavalry);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(nekoTe);
        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature damaging a player makes that player lose 1 life")
    void equippedCreatureDamagingPlayerCausesLifeLoss() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        attachNekoTe(player1, attacker);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Each attached Neko-Te causes one life loss per damage event")
    void multipleEquipmentEachCauseOneLifeLoss() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        attachNekoTe(player1, attacker);
        attachNekoTe(player1, attacker);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Resolving equip attaches Neko-Te to a creature")
    void resolvingEquipAttachesToTargetCreature() {
        Permanent nekoTe = harness.addToBattlefieldAndReturn(player1, new NekoTe());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(nekoTe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The damage trigger works when the equipped creature has a different controller")
    void equippedCreatureWithDifferentControllerStillTapsAndLocks() {
        Permanent cavalry = addCreatureReady(player2, new TakenosCavalry());
        attachNekoTe(player1, cavalry);
        Permanent target = addCreatureReady(player1, new GnarledMass());
        target.setAttacking(true);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        advanceToUpkeep(player1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Moving the Equipment before its trigger resolves does not remove the untap lock")
    void movingEquipmentBeforeTriggerResolvesDoesNotRemoveLock() {
        Permanent cavalry = addCreatureReady(player1, new TakenosCavalry());
        Permanent nekoTe = attachNekoTe(player1, cavalry);
        Permanent otherHost = addCreatureReady(player1, new GnarledMass());
        Permanent target = addCreatureReady(player2, new GnarledMass());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        nekoTe.setAttachedTo(otherHost.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damage from an unequipped creature does not trigger Neko-Te")
    void unequippedCreatureDamageDoesNotTapOrLock() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent host = addCreatureReady(player1, new GnarledMass());
        attachNekoTe(player1, host);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        target.setTapped(true);
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent attachNekoTe(Player player, Permanent host) {
        Permanent nekoTe = harness.addToBattlefieldAndReturn(player, new NekoTe());
        nekoTe.setAttachedTo(host.getId());
        return nekoTe;
    }
}
