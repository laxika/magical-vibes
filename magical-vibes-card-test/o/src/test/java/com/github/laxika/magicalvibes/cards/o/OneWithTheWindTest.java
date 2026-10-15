package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.s.SentinelTotem;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneWithTheWind.class, QueensBaySoldier.class, SentinelTotem.class})
class OneWithTheWindTest extends BaseCardTest {

    @Test
    @DisplayName("Casting One With the Wind puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(OneWithTheWind.class);
    }

    @Test
    @DisplayName("Resolving One With the Wind attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("One With the Wind")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OneWithTheWind());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OneWithTheWind());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and flying when One With the Wind is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OneWithTheWind());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        // Remove One With the Wind
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target a creature with One With the Wind")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with One With the Wind")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player1, new SentinelTotem());
        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Sentinel Totem");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("One With the Wind does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent otherBears = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new OneWithTheWind());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("One With the Wind can enchant and improve an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "One With the Wind");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Multiple copies stack their boosts and each independently grants flying")
    void multipleCopiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new OneWithTheWind(), new OneWithTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "One With the Wind")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "One With the Wind"));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }
}
