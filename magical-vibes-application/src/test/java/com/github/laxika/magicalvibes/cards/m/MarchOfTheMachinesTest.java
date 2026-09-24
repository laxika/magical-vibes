package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfTheMachines.class, AngelsFeather.class, GloriousAnthem.class,
        GrizzlyBears.class, IcyManipulator.class, LeoninScimitar.class, Ornithopter.class})
class MarchOfTheMachinesTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MarchOfTheMachines()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MarchOfTheMachines.class);
    }

    @Test
    @DisplayName("Resolving puts March of the Machines onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MarchOfTheMachines()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "March of the Machines");
    }

    // ===== Animating noncreature artifacts =====

    @Test
    @DisplayName("Noncreature artifact becomes a creature with P/T equal to mana value")
    void animatesNoncreatureArtifact() {
        // Angel's Feather costs {2}, so mana value = 2
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent feather = findPermanent(player1, "Angel's Feather");

        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated artifact does not gain any creature subtypes")
    void animatedArtifactDoesNotGainSubtypes() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent feather = findPermanent(player1, "Angel's Feather");

        // March of the Machines makes artifacts into creatures but does NOT grant creature subtypes
        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, feather)).isEmpty();
    }

    @Test
    @DisplayName("Icy Manipulator (cost {4}) becomes a 4/4 creature")
    void icyManipulatorBecomes4x4() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent icy = findPermanent(player1, "Icy Manipulator");

        assertThat(gqs.isCreature(gd, icy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, icy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, icy)).isEqualTo(4);
    }

    // ===== Does not affect creatures =====

    @Test
    @DisplayName("Does not change existing creature's P/T")
    void doesNotAffectCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not change an existing artifact creature's printed P/T")
    void doesNotAffectExistingArtifactCreature() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent ornithopter = findPermanent(player1, "Ornithopter");

        assertThat(gqs.isCreature(gd, ornithopter)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(2);
    }

    // ===== Animated artifacts benefit from creature buffs =====

    @Test
    @DisplayName("Animated artifacts benefit from Glorious Anthem")
    void animatedArtifactsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent feather = findPermanent(player1, "Angel's Feather");

        // Angel's Feather: mana value 2 + Glorious Anthem +1/+1 = 3/3
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(3);
    }

    // ===== Effect removed when March leaves =====

    @Test
    @DisplayName("Artifacts revert to non-creatures when March of the Machines leaves")
    void artifactsRevertWhenMarchLeaves() {
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent march = harness.addToBattlefieldAndReturn(player1, new MarchOfTheMachines());

        Permanent feather = findPermanent(player1, "Angel's Feather");

        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(march);

        assertThat(gqs.isCreature(gd, feather)).isFalse();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(0);
    }

    @Test
    @DisplayName("An Equipment animated by March cannot remain attached to a creature")
    void animatedEquipmentDetachesFromCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, scimitar)).isTrue();

        harness.runStateBasedActions();

        assertThat(scimitar.isAttached()).isFalse();
        assertThat(scimitar.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    // ===== Affects both players' artifacts =====

    @Test
    @DisplayName("Affects artifacts on both sides of the battlefield")
    void affectsBothPlayersArtifacts() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player2, new IcyManipulator());

        Permanent opponentIcy = findPermanent(player2, "Icy Manipulator");

        assertThat(gqs.isCreature(gd, opponentIcy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentIcy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentIcy)).isEqualTo(4);
    }

    // ===== Enchantments are not affected =====

    @Test
    @DisplayName("March of the Machines does not animate enchantments")
    void doesNotAnimateEnchantments() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent anthem = findPermanent(player1, "Glorious Anthem");

        assertThat(gqs.isCreature(gd, anthem)).isFalse();
    }
}

