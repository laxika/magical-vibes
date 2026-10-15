package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWarPaint.class, RuneclawBear.class, Manalith.class})
class GoblinWarPaintTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Goblin War Paint attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new GoblinWarPaint()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin War Paint")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and has haste")
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setSummoningSick(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and haste when Goblin War Paint leaves")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature is affected")
    void doesNotAffectOtherCreatures() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        Permanent otherBears = addCreatureReady(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new GoblinWarPaint()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Manalith");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opponent's creature can be enchanted and receives both benefits")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new GoblinWarPaint()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Goblin War Paint").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A newly controlled enchanted creature can attack immediately")
    void hasteAllowsImmediateAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new GoblinWarPaint()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Multiple copies stack their bonuses and the remaining copy still grants haste")
    void multipleCopiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        first.setAttachedTo(creature.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Aura does not resolve when its target has left the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new GoblinWarPaint()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Goblin War Paint");
        harness.assertInGraveyard(player1, "Goblin War Paint");
    }

    @Test
    @DisplayName("The Aura goes to its owner's graveyard when its enchanted creature leaves")
    void auraGoesToGraveyardWhenCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GoblinWarPaint());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Goblin War Paint");
        harness.assertInGraveyard(player1, "Goblin War Paint");
    }
}
