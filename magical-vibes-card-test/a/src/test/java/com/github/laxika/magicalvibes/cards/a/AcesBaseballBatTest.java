package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcesBaseballBat.class, DalekDrone.class, CybermanPatrol.class, AceFearlessRebel.class})
class AcesBaseballBatTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 and first strike only while attacking")
    void equippedCreatureGetsBoostAndAttackOnlyFirstStrike() {
        Permanent creature = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An attacking equipped creature must be blocked by a Dalek if able")
    void requiresDalekBlockerIfAble() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addCreatureReady(player2, new CybermanPatrol());
        Permanent dalek = addCreatureReady(player2, new DalekDrone());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("matching creature");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(dalek.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip legendary creature costs {1}, while ordinary equip costs {3}")
    void supportsBothEquipAbilities() {
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent legendaryCreature = addCreatureReady(player1, new AceFearlessRebel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, bat), 0, null,
                legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(bat.getAttachedTo()).isEqualTo(legendaryCreature.getId());

        Permanent ordinaryCreature = addCreatureReady(player1, new CybermanPatrol());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, bat), 1, null,
                ordinaryCreature.getId());
        harness.passBothPriorities();

        assertThat(bat.getAttachedTo()).isEqualTo(ordinaryCreature.getId());
    }

    @Test
    @DisplayName("The legendary equip ability rejects a nonlegendary creature")
    void legendaryEquipRejectsNonlegendaryCreature() {
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent creature = addCreatureReady(player1, new CybermanPatrol());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bat), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Assigning the only Dalek to another attacker cannot evade the blocking requirement")
    void dalekCannotBeDivertedToUnrestrictedAttacker() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent otherAttacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        otherAttacker.setAttacking(true);
        addCreatureReady(player2, new DalekDrone());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An available Dalek prevents leaving the equipped attacker unblocked")
    void availableDalekMustBlock() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DalekDrone());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("matching creature");
    }

    @Test
    @DisplayName("A tapped Dalek does not prevent blocking with a non-Dalek")
    void tappedDalekDoesNotRequireDalekBlock() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CybermanPatrol());
        Permanent dalek = addCreatureReady(player2, new DalekDrone());
        dalek.tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(dalek.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("With no Dalek available, the equipped attacker may remain unblocked")
    void noDalekAllowsNoBlocks() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CybermanPatrol());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Only one Dalek is required even when more are able to block")
    void oneOfMultipleDaleksSatisfiesRequirement() {
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent first = addCreatureReady(player2, new DalekDrone());
        Permanent second = addCreatureReady(player2, new DalekDrone());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The Bat does not grant first strike while its creature is blocking")
    void blockingCreatureDoesNotGainFirstStrike() {
        Permanent creature = addCreatureReady(player2, new CybermanPatrol());
        Permanent bat = addCreatureReady(player2, new AcesBaseballBat());
        bat.setAttachedTo(creature.getId());
        creature.setBlocking(true);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Detaching the Bat removes its boost and attacking first strike")
    void detachingRemovesBonuses() {
        Permanent creature = addCreatureReady(player1, new CybermanPatrol());
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        bat.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Neither equip ability can target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent creature = addCreatureReady(player2, new AceFearlessRebel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1,
                    battlefieldIndex(player1, bat), index, null, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(bat.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Ordinary equip cannot be activated with only two mana")
    void ordinaryEquipRequiresThreeMana() {
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent creature = addCreatureReady(player1, new CybermanPatrol());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, bat), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bat.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Legendary equip cannot be activated without mana")
    void legendaryEquipRequiresOneMana() {
        Permanent bat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent creature = addCreatureReady(player1, new AceFearlessRebel());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, bat), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bat.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("One Dalek may satisfy either of two competing Bat blocking requirements")
    void oneDalekCanBlockEitherEquippedAttacker() {
        Permanent firstAttacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent secondAttacker = addCreatureReady(player1, new CybermanPatrol());
        Permanent firstBat = addCreatureReady(player1, new AcesBaseballBat());
        Permanent secondBat = addCreatureReady(player2, new AcesBaseballBat());
        firstBat.setAttachedTo(firstAttacker.getId());
        secondBat.setAttachedTo(secondAttacker.getId());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        Permanent dalek = addCreatureReady(player2, new DalekDrone());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(battlefieldIndex(player2, dalek),
                        battlefieldIndex(player1, firstAttacker))));

        assertThat(dalek.isBlocking()).isTrue();
        assertThat(secondAttacker.isAttacking()).isTrue();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
