package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodforgedBattleAxe.class, GrizzlyBears.class, AirElemental.class})
class BloodforgedBattleAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage to a player creates a token copy")
    void combatDamageToPlayerCreatesTokenCopy() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bloodforged Battle-Axe"));
    }

    @Test
    @DisplayName("Blocked equipped creature does not create a token copy")
    void blockedCreatureDoesNotCreateTokenCopy() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isOne();
    }

    @Test
    @DisplayName("Equip pays two mana and attaches the Equipment")
    void equipAttachesAndBoostsCreature() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Token copy enters unattached and retains equip and the copy trigger")
    void tokenCanEquipAndCreateAnotherCopy() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Bloodforged Battle-Axe").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getAttachedTo()).isNull();
        assertThat(token.isTapped()).isFalse();
        axe.setAttachedTo(null);
        creature.setAttacking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token),
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(token.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        creature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isEqualTo(3);
    }

    @Test
    @DisplayName("Copy trigger resolves after the original Equipment leaves the battlefield")
    void copyIsCreatedAfterSourceLeavesBattlefield() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(axe);
        gd.playerGraveyards.get(player1.getId()).add(axe.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isOne();
        assertThat(findPermanent(player1, "Bloodforged Battle-Axe").getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Equipment controller creates the copy when an opposing equipped creature hits them")
    void equipmentControllerCreatesCopyForOpposingCreature() {
        Permanent axe = addCreatureReady(player1, new BloodforgedBattleAxe());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isEqualTo(2);
        assertThat(countPermanents(player2, "Bloodforged Battle-Axe")).isZero();
    }
}
