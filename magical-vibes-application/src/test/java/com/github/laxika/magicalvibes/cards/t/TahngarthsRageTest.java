package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TahngarthsRage.class, BayouDragonfly.class, TrainedArmodon.class, CursedScroll.class})
class TahngarthsRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature that isn't attacking gets -2/-1")
    void notAttackingGetsPenalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BayouDragonfly());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TahngarthsRage());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isZero();
    }

    @Test
    @DisplayName("Enchanted creature gets +3/+0 while it's attacking")
    void attackingGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BayouDragonfly());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TahngarthsRage());
        aura.setAttachedTo(creature.getId());

        creature.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        creature.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isZero();
    }

    @Test
    @DisplayName("Creature returns to base stats when Tahngarth's Rage leaves")
    void statsRestoredWhenAuraRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BayouDragonfly());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TahngarthsRage());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tahngarth's Rage can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new TahngarthsRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        harness.setHand(player1, List.of(new TahngarthsRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
