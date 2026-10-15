package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicStrength.class, RuneclawBear.class, DragonsClaw.class, Mountain.class})
class VolcanicStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Volcanic Strength puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Volcanic Strength attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Volcanic Strength")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has mountainwalk")
    void enchantedCreatureHasMountainwalk() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and mountainwalk when Volcanic Strength is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isTrue();

        // Remove Volcanic Strength
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Can target a creature with Volcanic Strength")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Volcanic Strength")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Dragon's Claw");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Volcanic Strength does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent otherBears = addCreatureReady(player1, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Volcanic Strength can enchant and boost an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Volcanic Strength");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Volcanic Strength goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new VolcanicStrength()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof VolcanicStrength);
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking even when the defending Mountain is tapped")
    void cannotBeBlockedWithDefendingMountain() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.tap();
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountainwalk allows blocking when the defending player has no Mountain")
    void canBeBlockedWithoutDefendingMountain() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk does not prevent blocking when only the attacker controls a Mountain")
    void attackingPlayersMountainDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VolcanicStrength());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
