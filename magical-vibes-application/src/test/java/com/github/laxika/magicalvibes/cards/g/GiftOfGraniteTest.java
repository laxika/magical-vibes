package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.n.NewBenalia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfGranite.class, NessianCourser.class, NewBenalia.class})
class GiftOfGraniteTest extends BaseCardTest {

    @Test
    @DisplayName("Gift of Granite can be cast during the opponent's declare attackers step")
    void canBeCastAtInstantSpeed() {
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiftOfGranite()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, creature.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Gift of Granite gives the enchanted creature +0/+2")
    void givesEnchantedCreatureToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getAttachedTo() != null
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Gift of Granite can enchant and boost an opponent's creature")
    void boostsOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new NessianCourser());

        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gift of Granite stops boosting the creature when it leaves the battlefield")
    void effectStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new NessianCourser());
        Permanent aura = new Permanent(new GiftOfGranite());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gift of Granite fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Gift of Granite cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NewBenalia());
        harness.setHand(player1, List.of(new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Multiple Gifts of Granite stack and boost only their enchanted creature")
    void multipleCopiesBoostOnlyEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new NessianCourser());
        Permanent other = addCreatureReady(player1, new NessianCourser());
        Permanent opposing = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new GiftOfGranite(), new GiftOfGranite()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);
    }
}
