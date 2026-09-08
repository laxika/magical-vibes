package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeshracsRite.class, BalduvianBears.class, Swamp.class, ZuranOrb.class})
class LeshracsRiteTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Leshrac's Rite puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new LeshracsRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Leshrac's Rite attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new LeshracsRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leshrac's Rite")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Leshrac's Rite can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new LeshracsRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.SWAMPWALK)).isTrue();
    }

    // ===== Grants swampwalk =====

    @Test
    @DisplayName("Enchanted creature has swampwalk")
    void enchantedCreatureHasSwampwalk() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent ritePerm = harness.addToBattlefieldAndReturn(player1, new LeshracsRite());
        ritePerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot be blocked while defending player controls a Swamp")
    void enchantedCreatureCannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent rite = harness.addToBattlefieldAndReturn(player1, new LeshracsRite());
        rite.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked while defending player controls no Swamp")
    void enchantedCreatureCanBeBlockedWithoutDefendingSwamp() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent rite = harness.addToBattlefieldAndReturn(player1, new LeshracsRite());
        rite.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses swampwalk when Leshrac's Rite is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent ritePerm = harness.addToBattlefieldAndReturn(player1, new LeshracsRite());
        ritePerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SWAMPWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(ritePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SWAMPWALK)).isFalse();
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Leshrac's Rite does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent otherBears = addCreatureReady(player1, new BalduvianBears());

        Permanent ritePerm = harness.addToBattlefieldAndReturn(player1, new LeshracsRite());
        ritePerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.SWAMPWALK)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Leshrac's Rite")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new LeshracsRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Zuran Orb");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
