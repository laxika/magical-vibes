package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KomainuBattleArmor.class, GrizzlyBears.class})
class KomainuBattleArmorTest extends BaseCardTest {

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
