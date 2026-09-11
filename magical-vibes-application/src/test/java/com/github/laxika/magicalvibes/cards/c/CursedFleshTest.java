package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ErraticPortal;
import com.github.laxika.magicalvibes.cards.g.Grollub;
import com.github.laxika.magicalvibes.cards.t.ThopterSquadron;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CursedFlesh.class, CrashingBoars.class, Grollub.class, ErraticPortal.class,
        ThopterSquadron.class})
class CursedFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1 and fear")
    void enchantedCreatureGetsMinusOneMinusOneAndFear() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrashingBoars());

        harness.setHand(player1, List.of(new CursedFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creature with Cursed Flesh cannot be blocked by a normal creature")
    void cannotBeBlockedByNormalCreature() {
        enchantedAttackingCreature();

        addCreatureReady(player2, new CrashingBoars());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Creature with Cursed Flesh can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        enchantedAttackingCreature();

        Permanent blocker = addCreatureReady(player2, new Grollub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creature with Cursed Flesh can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        enchantedAttackingCreature();

        Permanent blocker = addCreatureReady(player2, new ThopterSquadron());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Removing Cursed Flesh restores the creature")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new CrashingBoars());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CursedFlesh());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ErraticPortal());
        harness.setHand(player1, List.of(new CursedFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchantedAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new CrashingBoars());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CursedFlesh());
        aura.setAttachedTo(attacker.getId());

        return attacker;
    }

}
