package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyblinderStaff.class, GrizzlyBears.class, SuntailHawk.class})
class SkyblinderStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = equipStaffTo(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature can't be blocked by a creature with flying")
    void cannotBeBlockedByFlier() {
        Permanent attacker = equipStaffTo(new GrizzlyBears());
        Permanent blocker = addBlocker(new SuntailHawk());

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature can still be blocked by a creature without flying")
    void canBeBlockedByNonFlier() {
        Permanent attacker = equipStaffTo(new GrizzlyBears());
        Permanent blocker = addBlocker(new GrizzlyBears());

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unattached Skyblinder Staff does not stop fliers from blocking")
    void unattachedStaffDoesNotRestrictBlocks() {
        addStaffReady(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent blocker = addBlocker(new SuntailHawk());

        attacker.setAttacking(true);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reequipping moves both the boost and the flying blocker restriction")
    void reequippingMovesBothBenefits() {
        Permanent original = equipStaffTo(new GrizzlyBears());
        Permanent next = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        next.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, next.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, next)).isEqualTo(3);
        Permanent blocker = addBlocker(new SuntailHawk());
        assertThatThrownBy(() -> declareBlock(blocker, next))
                .isInstanceOf(IllegalStateException.class);
        next.setAttacking(false);
        declareBlock(blocker, original);
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent staff = addStaffReady(player1);
        Permanent opponentCreature = addBlocker(new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
    }

    private Permanent equipStaffTo(Card creatureCard) {
        addStaffReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, creatureCard);
        creature.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private Permanent addStaffReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SkyblinderStaff());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addBlocker(Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player2, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
