package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaleficScythe.class, GrizzlyBears.class, Deathmark.class})
class MaleficScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a soul counter")
    void entersWithSoulCounter() {
        harness.setHand(player1, List.of(new MaleficScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent scythe = findPermanent(player1, "Malefic Scythe");
        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each soul counter")
    void equippedCreatureGetsBoostPerSoulCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        scythe.setCounterCount(CounterType.SOUL, 3);
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Adds a soul counter when the equipped creature dies")
    void addsSoulCounterWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        scythe.setCounterCount(CounterType.SOUL, 1);
        scythe.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches to a creature you control")
    void equipAttaches() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Only soul counters on the Equipment contribute to the bonus")
    void bonusTracksOnlySoulCountersOnEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        scythe.setAttachedTo(creature.getId());
        scythe.setCounterCount(CounterType.CHARGE, 4);
        creature.setCounterCount(CounterType.SOUL, 5);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        scythe.setCounterCount(CounterType.SOUL, 2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        scythe.setCounterCount(CounterType.SOUL, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Re-equipping moves the bonus and retains soul counters")
    void reequippingMovesBonusAndRetainsCounters() {
        Permanent scythe = harness.enterBattlefieldAndReturn(player1, new MaleficScythe());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        scythe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("An unrelated creature dying does not add a soul counter")
    void unrelatedCreatureDeathDoesNotAddCounter() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = harness.enterBattlefieldAndReturn(player1, new MaleficScythe());
        scythe.setAttachedTo(equipped.getId());
        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, other.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death of an opponent-controlled equipped creature adds a counter only on resolution")
    void opponentsEquippedCreatureDeathTriggersForEquipmentController() {
        Permanent scythe = harness.enterBattlefieldAndReturn(player1, new MaleficScythe());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        scythe.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new MaleficScythe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
