package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Greatsword.class, RuneclawBear.class})
class GreatswordTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Greatsword and gives the creature +3/+0")
    void equipAttachesAndBoosts() {
        harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent greatsword = findPermanent(player1, "Greatsword");
        assertThat(greatsword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} costs three mana")
    void equipCostsThreeMana() {
        harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot equip with only two mana")
    void cannotEquipWithTwoMana() {
        harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Creature loses the boost when Greatsword leaves the battlefield")
    void boostEndsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent greatsword = harness.addToBattlefieldAndReturn(player1, new Greatsword());
        greatsword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(greatsword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Greatsword does not boost unequipped creatures")
    void doesNotBoostOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent other = addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefieldAndReturn(player1, new Greatsword()).setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    void reEquipMovesBoostOnlyOnResolution() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(sword.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReEquipLeavesOriginalAttachment() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new Greatsword());
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);

        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
