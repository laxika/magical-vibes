package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IonasBlessing.class, GrizzlyBears.class, FountainOfYouth.class})
class IonasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Iona's Blessing attaches to a creature and grants its static abilities")
    void grantsStaticAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IonasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("The enchanted creature can block an additional creature")
    void enchantedCreatureCanBlockAdditionalCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new IonasBlessing());
        aura.setAttachedTo(blocker.getId());

        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int firstAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker);
        int secondAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, firstAttackerIndex),
                new BlockerAssignment(blockerIndex, secondAttackerIndex)
        ));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(
                firstAttackerIndex, secondAttackerIndex);
    }

    @Test
    @DisplayName("Iona's Blessing stops affecting the creature when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IonasBlessing());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Iona's Blessing cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new IonasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opponent's creature receives the additional block regardless of who controls the Aura")
    void opponentCreatureCanBlockAdditionalCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IonasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.VIGILANCE)).isTrue();

        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int firstIndex = gd.playerBattlefields.get(player1.getId()).indexOf(first);
        int secondIndex = gd.playerBattlefields.get(player1.getId()).indexOf(second);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, firstIndex),
                new BlockerAssignment(blockerIndex, secondIndex)));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(firstIndex, secondIndex);
    }

    @Test
    @DisplayName("Vigilance lets only the enchanted creature attack without tapping")
    void enchantedCreatureAttacksWithoutTapping() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IonasBlessing());
        aura.setAttachedTo(enchanted.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two copies allow three blocks, and removing one restores the two-block limit")
    void additionalBlocksStackAndStopWhenAuraLeaves() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player2, new IonasBlessing());
        firstAura.setAttachedTo(blocker.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player2, new IonasBlessing());
        secondAura.setAttachedTo(blocker.getId());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        }
        prepareDeclareBlockers(player1);
        List<BlockerAssignment> blocks = List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2));

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(6);
        gd.playerBattlefields.get(player2.getId()).remove(secondAura);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, blocks))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");

        gd.playerBattlefields.get(player2.getId()).add(secondAura);
        gs.declareBlockers(gd, player2, blocks);
        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }
}
