package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.p.PrimalAmulet;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swashbuckling.class, QueensBaySoldier.class, PrimalAmulet.class})
class SwashbucklingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Swashbuckling puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Swashbuckling.class);
    }

    @Test
    @DisplayName("Resolving Swashbuckling attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swashbuckling")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has haste")
    void enchantedCreatureHasHaste() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and haste when Swashbuckling is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isTrue();

        // Remove Swashbuckling
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target a creature with Swashbuckling")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Swashbuckling")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrimalAmulet());
        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Swashbuckling does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new QueensBaySoldier());

        Permanent otherBears = addCreatureReady(player1, new QueensBaySoldier());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Swashbuckling can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Swashbuckling").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Swashbucklings stack their boosts and removing one preserves the other")
    void multipleAurasStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        first.setAttachedTo(creature.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Swashbuckling());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Swashbuckling goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof Swashbuckling);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Swashbuckling);
    }
    @Test
    @DisplayName("Swashbuckling lets a newly entered creature attack immediately")
    void hasteAllowsImmediateAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new Swashbuckling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(creature.isTapped()).isTrue();
    }
}
