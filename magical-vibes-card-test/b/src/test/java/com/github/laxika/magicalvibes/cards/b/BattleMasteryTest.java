package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.d.DolmenGate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleMastery.class, DeeptreadMerrow.class, DolmenGate.class})
class BattleMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Battle Mastery puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());

        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Battle Mastery");
    }

    @Test
    @DisplayName("Resolving Battle Mastery attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());

        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Battle Mastery")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has double strike")
    void enchantedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());

        attachBattleMastery(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature deals combat damage in both phases")
    void doubleStrikeDealsDamageInBothPhases() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());
        creature.setAttacking(true);

        attachBattleMastery(creature);

        resolveCombat();

        // Deeptread Merrow (2/1) with double strike deals 2 + 2 = 4 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A blocked double striker kills its blocker before retaliation without damaging the player")
    void blockedDoubleStrikerDoesNotDamagePlayerAfterKillingBlocker() {
        Permanent attacker = addCreatureReady(player1, new DeeptreadMerrow());
        addCreatureReady(player2, new DeeptreadMerrow());
        attachBattleMastery(attacker);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Deeptread Merrow");
        harness.assertInGraveyard(player2, "Deeptread Merrow");
        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Battle Mastery gives an opposing blocker first-strike combat damage")
    void enchantedOpposingBlockerKillsAttackerBeforeRetaliation() {
        addCreatureReady(player1, new DeeptreadMerrow());
        Permanent blocker = addCreatureReady(player2, new DeeptreadMerrow());
        attachBattleMastery(blocker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Deeptread Merrow");
        harness.assertOnBattlefield(player2, "Deeptread Merrow");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creature loses double strike when Battle Mastery is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());

        Permanent battleMasteryPerm = attachBattleMastery(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(battleMasteryPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Battle Mastery does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());
        Permanent otherCreature = addCreatureReady(player1, new DeeptreadMerrow());

        attachBattleMastery(creature);

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Battle Mastery fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());

        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Battle Mastery");
        harness.assertNotOnBattlefield(player1, "Battle Mastery");
    }

    @Test
    @DisplayName("Can target a creature with Battle Mastery")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new DeeptreadMerrow());
        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Battle Mastery can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new DeeptreadMerrow());
        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Battle Mastery")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Battle Mastery")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DolmenGate());
        harness.setHand(player1, List.of(new BattleMastery()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachBattleMastery(Permanent creature) {
        Permanent battleMastery = harness.addToBattlefieldAndReturn(player1, new BattleMastery());
        battleMastery.setAttachedTo(creature.getId());
        return battleMastery;
    }
}
