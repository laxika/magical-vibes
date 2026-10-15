package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheReaverCleaver.class, GrizzlyBears.class, JaceBeleren.class})
class TheReaverCleaverTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void createsTreasureTokensEqualToCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(3);
    }

    @Test
    void canEquipForThreeMana() {
        Permanent cleaver = addCleaverReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void createsTreasureFromCombatDamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        creature.setAttacking(true);
        creature.setAttackTarget(planeswalker.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void creatureControllerCreatesTreasureWhenEquipmentHasAnotherController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player2);
        cleaver.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void unattachedEquipmentDoesNotBoostOrCreateTreasure() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCleaverReady(player1);
        creature.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private Permanent addCleaverReady(Player player) {
        return addCreatureReady(player, new TheReaverCleaver());
    }
}
