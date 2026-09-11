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
        Permanent aura = new Permanent(new IonasBlessing());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

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
        Permanent aura = new Permanent(new IonasBlessing());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Iona's Blessing cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.setHand(player1, List.of(new IonasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
