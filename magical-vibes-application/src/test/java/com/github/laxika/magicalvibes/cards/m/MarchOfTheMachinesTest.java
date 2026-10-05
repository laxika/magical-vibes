package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfTheMachines.class, AngelsFeather.class, GloriousAnthem.class,
        FountainOfYouth.class, GrizzlyBears.class, IcyManipulator.class, LeoninScimitar.class,
        Ornithopter.class, MycosynthLattice.class})
class MarchOfTheMachinesTest extends BaseCardTest {

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

    @Test
    @DisplayName("Noncreature artifact becomes a creature with P/T equal to mana value")
    void animatesNoncreatureArtifact() {
        // Angel's Feather costs {2}, so mana value = 2
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.isArtifact(gd, feather)).isTrue();
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(2);
    }

    @Test
    @DisplayName("A zero-mana noncreature artifact becomes 0/0 and dies to state-based actions")
    void zeroManaArtifactDiesToStateBasedActions() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gqs.isArtifact(gd, fountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fountain)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isZero();

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Animated artifact does not gain any creature subtypes")
    void animatedArtifactDoesNotGainSubtypes() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        // March of the Machines makes artifacts into creatures but does NOT grant creature subtypes
        assertThat(gqs.isCreature(gd, feather)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, feather)).isEmpty();
    }

    @Test
    @DisplayName("Icy Manipulator (cost {4}) becomes a 4/4 creature")
    void icyManipulatorBecomes4x4() {
        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, icy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, icy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, icy)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not change existing creature's P/T")
    void doesNotAffectCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not change an existing artifact creature's printed P/T")
    void doesNotAffectExistingArtifactCreature() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, ornithopter)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated artifacts benefit from Glorious Anthem")
    void animatedArtifactsBenefitFromAnthem() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new GloriousAnthem());

        // Angel's Feather: mana value 2 + Glorious Anthem +1/+1 = 3/3
        assertThat(gqs.getEffectivePower(gd, feather)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, feather)).isEqualTo(3);
    }

    @Test
    @DisplayName("Artifacts revert to non-creatures when March of the Machines leaves")
    void artifactsRevertWhenMarchLeaves() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Permanent march = harness.addToBattlefieldAndReturn(player1, new MarchOfTheMachines());

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

    @Test
    @DisplayName("Affects artifacts on both sides of the battlefield")
    void affectsBothPlayersArtifacts() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent opponentIcy = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        assertThat(gqs.isCreature(gd, opponentIcy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentIcy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentIcy)).isEqualTo(4);
    }

    @Test
    @DisplayName("March of the Machines does not animate enchantments")
    void doesNotAnimateEnchantments() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, anthem)).isFalse();
    }

    @Test
    @DisplayName("A zero-mana artifact survives as a 1/1 with Glorious Anthem")
    void zeroManaArtifactSurvivesWithAnthem() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        assertThat(gqs.getEffectivePower(gd, fountain)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("March animates itself when Mycosynth Lattice makes it an artifact")
    void animatesItselfWithMycosynthLattice(boolean latticeEntersFirst) {
        if (latticeEntersFirst) {
            harness.addToBattlefield(player1, new MycosynthLattice());
        }
        Permanent march = harness.addToBattlefieldAndReturn(player1, new MarchOfTheMachines());
        if (!latticeEntersFirst) {
            harness.addToBattlefield(player1, new MycosynthLattice());
        }

        assertThat(gqs.isArtifact(gd, march)).isTrue();
        assertThat(gqs.isCreature(gd, march)).isTrue();
        assertThat(gqs.getEffectivePower(gd, march)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, march)).isEqualTo(4);
    }
}
