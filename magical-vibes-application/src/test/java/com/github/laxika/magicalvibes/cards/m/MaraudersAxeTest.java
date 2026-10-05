package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaraudersAxe.class, GreenwoodSentinel.class})
class MaraudersAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving equip attaches Marauder's Axe to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature loses the boost when Marauder's Axe leaves the battlefield")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        axe.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(axe);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void reEquipMovesBonusOnlyWhenAbilityResolves() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent first = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent second = addCreatureReady(player1, new GreenwoodSentinel());
        axe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void illegalEquipTargetLeavesExistingAttachmentIntact() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent first = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent second = addCreatureReady(player1, new GreenwoodSentinel());
        axe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());

        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNonCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWithOnlyOneMana() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        harness.addToBattlefield(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipDuringOpponentTurn() {
        harness.addToBattlefield(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWithAbilityOnStack() {
        harness.addToBattlefield(player1, new MaraudersAxe());
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack");
        harness.passBothPriorities();
    }
}
