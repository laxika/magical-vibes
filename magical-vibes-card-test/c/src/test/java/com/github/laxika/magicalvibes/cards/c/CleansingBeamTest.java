package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariBrownscale;
import com.github.laxika.magicalvibes.cards.g.GolgariRotwurm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Moroii;
import com.github.laxika.magicalvibes.cards.p.PrivilegedPosition;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleansingBeam.class, ViashinoFangtail.class, SnappingDrake.class, Island.class,
        GolgariRotwurm.class, GolgariBrownscale.class, Moroii.class, GlassGolem.class,
        PrivilegedPosition.class})
class CleansingBeamTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the target and every creature sharing a color with it")
    void damagesTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages creatures sharing either color with a multicolored target")
    void damagesCreaturesSharingEitherColorWithMulticoloredTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariRotwurm());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Moroii());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(greenCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(blackCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A colorless target damages only itself")
    void colorlessTargetOnlyDamagesItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(otherColorlessCreature.getMarkedDamage()).isZero();
        assertThat(coloredCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Determines color-sharing creatures when the spell resolves")
    void determinesColorSharingCreaturesOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        Permanent creatureEnteringBeforeResolution =
                harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(creatureEnteringBeforeResolution.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage other creatures if the target leaves before resolution")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(matchingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Color sharing does not spread through other multicolored creatures")
    void doesNotSpreadThroughOtherMulticoloredCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariBrownscale());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        Permanent nonmatchingCreature = harness.addToBattlefieldAndReturn(player2, new Moroii());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(nonmatchingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damages a color-sharing creature with hexproof without targeting it")
    void damagesColorSharingCreatureWithHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.addToBattlefield(player2, new PrivilegedPosition());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals lethal damage to a colorless target without damaging other colorless creatures")
    void killsColorlessTargetOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Glass Golem");
        harness.assertInGraveyard(player1, "Glass Golem");
        harness.assertOnBattlefield(player2, "Glass Golem");
        assertThat(otherCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new CleansingBeam()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
