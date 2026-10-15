package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KomainuBattleArmor.class, GrizzlyBears.class})
class KomainuBattleArmorTest extends BaseCardTest {

    @Test
    void reconfigureCannotTargetItself() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, armor.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent opponent = addCreatureReady(player2, new KomainuBattleArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void unattachCannotBeActivatedWhileUnattached() {
        addCreatureReady(player1, new KomainuBattleArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent creature = addCreatureReady(player1, new KomainuBattleArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        armor.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void goadAffectsCreaturesPresentAtResolutionButNotLaterArrivalsOrControllersCreatures() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        armor.setAttacking(true);
        armor.setAttackTarget(player2.getId());
        Permanent original = addCreatureReady(player2, new KomainuBattleArmor());

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        Permanent beforeResolution = addCreatureReady(player2, new KomainuBattleArmor());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player2, new KomainuBattleArmor());

        assertThat(als.getMustAttackRequirementCount(gd, original)).isOne();
        assertThat(als.getMustAttackRequirementCount(gd, beforeResolution)).isOne();
        assertThat(als.getMustAttackRequirementCount(gd, afterResolution)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, armor)).isZero();

    }

    @Test
    void reconfigureCanMoveBetweenCreaturesAndDetachingRemovesTheBoost() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent first = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent second = addCreatureReady(player1, new KomainuBattleArmor());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, armor)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void equippedCreatureGetsBoostAndMenaceWhileArmorStopsBeingACreature() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.isCreature(gd, armor)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheArmor() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, armor)).isTrue();
    }

    @Test
    void combatDamageByTheArmorGoadsAllCreaturesControlledByTheDamagedPlayer() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        armor.setAttacking(true);
        armor.setAttackTarget(player2.getId());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, first)).isOne();
        assertThat(als.getMustAttackRequirementCount(gd, second)).isOne();
    }

    @Test
    void combatDamageByEquippedCreatureGoadsAllCreaturesControlledByTheDamagedPlayer() {
        Permanent armor = addCreatureReady(player1, new KomainuBattleArmor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        armor.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, first)).isOne();
        assertThat(als.getMustAttackRequirementCount(gd, second)).isOne();
    }
}
