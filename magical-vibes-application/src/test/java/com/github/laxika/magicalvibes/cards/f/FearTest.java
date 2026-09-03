package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BrassMan;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SoldeviGolem;
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

@CardUsed({Fear.class, BalduvianBears.class, MoorFiend.class, SoldeviGolem.class, Mountain.class, GrizzlyBears.class, DrudgeSkeletons.class, BrassMan.class})
class FearTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fear puts it on the stack")
    void castingPutsOnStack() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Fear attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Fear
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("Fear can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent fear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof Fear)
                .findFirst()
                .orElseThrow();
        assertThat(fear.isAttached()).isTrue();
        assertThat(fear.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has fear")
    void enchantedCreatureHasFear() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creature with fear cannot be blocked by non-black non-artifact creature")
    void cannotBeBlockedByNonBlackNonArtifactCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(attacker.getId());

        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Creature with fear can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(attacker.getId());

        addCreatureReady(player2, new MoorFiend());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Creature with fear can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(attacker.getId());

        addCreatureReady(player2, new SoldeviGolem());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Creature loses fear when Fear aura is removed")
    void effectsStopWhenRemoved() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fear);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        Permanent otherCreature = addCreatureReady(player1, new BalduvianBears());

        Permanent fear = harness.addToBattlefieldAndReturn(player1, new Fear());
        fear.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Fear);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Fear);
    }

    @Test
    @DisplayName("Creature with fear keyword granted directly cannot be blocked by non-black non-artifact creature")
    void innateKeywordBlockingRestriction() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        attacker.getGrantedKeywords().add(Keyword.FEAR);

        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new BalduvianBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
