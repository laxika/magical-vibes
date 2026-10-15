package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinAnarchomancer;
import com.github.laxika.magicalvibes.cards.g.GristTheHungerTide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningSpear.class, GoblinAnarchomancer.class, GristTheHungerTide.class})
class LightningSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1);
        Permanent spear = addSpear(player1);
        spear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {1} attaches Lightning Spear to a creature")
    void equipAttaches() {
        Permanent spear = addSpear(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing Lightning Spear deals 3 damage to a creature")
    void sacrificesAndDealsDamageToCreature() {
        addSpear(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        addDamageMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Lightning Spear");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Anarchomancer");
    }

    @Test
    @DisplayName("Sacrificing Lightning Spear deals 3 damage to a player")
    void sacrificesAndDealsDamageToPlayer() {
        addSpear(player1);
        harness.setLife(player2, 20);
        addDamageMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Lightning Spear");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Sacrificing an equipped Spear removes its bonuses immediately")
    void sacrificeRemovesBonusesBeforeDamageResolves() {
        Permanent spear = addSpear(player1);
        Permanent creature = addCreatureReady(player1);
        spear.setAttachedTo(creature.getId());
        addDamageMana();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Lightning Spear");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Re-equipping moves both bonuses when the equip ability resolves")
    void reEquipMovesBonusesOnResolution() {
        Permanent spear = addSpear(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        spear.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, second.getId());

        assertThat(spear.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent spear = addSpear(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(spear.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lightning Spear deals exactly three damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        addSpear(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GristTheHungerTide());
        target.setCounterCount(CounterType.LOYALTY, 4);
        addDamageMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertInGraveyard(player1, "Lightning Spear");
    }

    @Test
    @DisplayName("Lightning Spear can damage its controller")
    void canDamageController() {
        addSpear(player1);
        addDamageMana();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    private Permanent addSpear(Player player) {
        return addCreatureReady(player, new LightningSpear());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GoblinAnarchomancer());
    }

    private void addDamageMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
