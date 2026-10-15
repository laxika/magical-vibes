package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpicProportions.class, WoodlandChangeling.class, SpringleafDrum.class})
class EpicProportionsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Epic Proportions puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Epic Proportions");
    }

    @Test
    @DisplayName("Resolving Epic Proportions attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Epic Proportions")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +5/+5")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new EpicProportions());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(7);
    }

    @Test
    @DisplayName("Enchanted creature has trample")
    void enchantedCreatureHasTrample() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new EpicProportions());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and trample when Epic Proportions is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new EpicProportions());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Epic Proportions")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        Permanent artifact = findPermanent(player1, "Springleaf Drum");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Epic Proportions does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new WoodlandChangeling());

        Permanent otherBears = addCreatureReady(player1, new WoodlandChangeling());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new EpicProportions());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows enchanting an opponent's creature during their upkeep")
    void flashAllowsEnchantingOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EpicProportions()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Epic Proportions");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Epic Proportions goes to the graveyard if its target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Epic Proportions");
        harness.assertInGraveyard(player1, "Epic Proportions");
    }

    @Test
    @DisplayName("Epic Proportions goes to its owner's graveyard when the enchanted creature leaves")
    void auraGoesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Epic Proportions");
        harness.assertInGraveyard(player1, "Epic Proportions");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card instanceof EpicProportions);
    }

    @Test
    @DisplayName("Granted trample deals excess damage through a blocker")
    void grantedTrampleDealsExcessDamage() {
        Permanent attacker = addCreatureReady(player1, new WoodlandChangeling());
        Permanent blocker = addCreatureReady(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EpicProportions()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Woodland Changeling");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }
}
