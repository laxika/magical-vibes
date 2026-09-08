package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrystalRod;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({Burrowing.class, GrizzlyBears.class, CrystalRod.class, Mountain.class})
class BurrowingTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Burrowing puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Burrowing()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Burrowing attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Burrowing()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burrowing")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Grants mountainwalk =====

    @Test
    @DisplayName("Enchanted creature has mountainwalk")
    void enchantedCreatureHasMountainwalk() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isTrue();
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses mountainwalk when Burrowing is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.MOUNTAINWALK)).isFalse();
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Burrowing does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Burrowing can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Burrowing()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, opponentBears.getId(), null);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Burrowing");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(opponentBears.getId());
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking while the defending player controls a Mountain")
    void mountainwalkPreventsBlockingWithMountain() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountainwalk still allows blocking when the defending player controls no Mountain")
    void mountainwalkAllowsBlockingWithoutMountain() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new Burrowing());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Burrowing")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CrystalRod());
        harness.setHand(player1, List.of(new Burrowing()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Crystal Rod");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
