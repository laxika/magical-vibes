package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PryingBlade.class, GrizzlyBears.class, SerraAngel.class, EnsoulArtifact.class})
class PryingBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Prying Blade has equip {2} ability")
    void hasEquipAbility() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(blade.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(blade.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature loses boost when Prying Blade is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(blade);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Treasure token when equipped creature deals combat damage to a player")
    void createsTreasureTokenOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
    }

    @Test
    @DisplayName("Treasure token has sacrifice-for-mana ability")
    void treasureTokenHasManaAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Blocker with 4 toughness survives the 3 power creature
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).isEmpty();
    }

    @Test
    @DisplayName("Prying Blade can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        blade.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Treasure belongs to the Blade controller when an opponent controls the equipped creature")
    void treasureBelongsToEquipmentController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("An unattached Blade does not trigger for another creature's combat damage")
    void unattachedBladeDoesNotTrigger() {
        addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({PryingBlade.class, EnsoulArtifact.class})
    @DisplayName("An animated Prying Blade does not create Treasure for its own combat damage")
    void animatedBladeDoesNotTriggerForItsOwnDamage() {
        Permanent blade = addBladeReady(player1);
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, blade.getId());
        harness.passBothPriorities();
        blade.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires two mana")
    void cannotEquipWithOnlyOneMana() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addBladeReady(Player player) {
        return addCreatureReady(player, new PryingBlade());
    }
}
