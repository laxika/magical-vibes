package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fear.class, GrizzlyBears.class, DrudgeSkeletons.class, DancingScimitar.class,
        HowlingMine.class, Mountain.class})
class FearTest extends BaseCardTest {
    @Test
    @DisplayName("Casting Fear puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Fear attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Fear
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Fear can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isTrue();
    }
    @Test
    @DisplayName("Enchanted creature has fear")
    void enchantedCreatureHasFear() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isTrue();
    }
    @Test
    @DisplayName("Creature with fear cannot be blocked by non-black non-artifact creature")
    void cannotBeBlockedByNonBlackNonArtifactCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = addCreatureReady(player1, new GrizzlyBears());
        attackerPerm.setAttacking(true);

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());

        // Blocker: GrizzlyBears (green, non-artifact) on player2
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Creature with fear can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = addCreatureReady(player1, new GrizzlyBears());
        attackerPerm.setAttacking(true);

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());

        // Blocker: Drudge Skeletons (black creature) on player2
        addCreatureReady(player2, new DrudgeSkeletons());

        prepareDeclareBlockers();

        // declareBlockers succeeds without throwing — black creature can block fear
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Creature with fear can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = addCreatureReady(player1, new GrizzlyBears());
        attackerPerm.setAttacking(true);

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());

        // Blocker: Dancing Scimitar (artifact creature) on player2
        addCreatureReady(player2, new DancingScimitar());

        prepareDeclareBlockers();

        // declareBlockers succeeds without throwing — artifact creature can block fear
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Creature with fear cannot be blocked by a noncreature artifact")
    void cannotBeBlockedByNonCreatureArtifact() {
        Permanent attackerPerm = addCreatureReady(player1, new GrizzlyBears());
        attackerPerm.setAttacking(true);

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());

        harness.addToBattlefield(player2, new HowlingMine());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
    @Test
    @DisplayName("Creature loses fear when Fear aura is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());

        // Verify fear is active
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isTrue();

        // Remove Fear aura
        gd.playerBattlefields.get(player1.getId()).remove(fearPerm);

        // Verify fear is gone
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isFalse();
    }
    @Test
    @DisplayName("Fear does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent fearPerm = harness.addToBattlefieldAndReturn(player1, new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not have fear
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FEAR)).isFalse();
    }
    @Test
    @DisplayName("Fear fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        // Fear should go to graveyard, not battlefield
        harness.assertInGraveyard(player1, "Fear");
        harness.assertNotOnBattlefield(player1, "Fear");
    }
    @Test
    @DisplayName("Creature with fear keyword granted directly cannot be blocked by non-black non-artifact creature")
    void innateKeywordBlockingRestriction() {
        // Simulate a creature that has fear as an innate keyword
        Permanent attackerPerm = addCreatureReady(player1, new GrizzlyBears());
        attackerPerm.setAttacking(true);
        attackerPerm.getGrantedKeywords().add(Keyword.FEAR);

        // Blocker: GrizzlyBears (green) on player2
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }
    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
