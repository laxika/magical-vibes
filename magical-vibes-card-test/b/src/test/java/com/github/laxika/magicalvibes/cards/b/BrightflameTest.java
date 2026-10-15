package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.Junktroller;
import com.github.laxika.magicalvibes.cards.p.PalisadeGiant;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brightflame.class, BenevolentAncestor.class, BorosSwiftblade.class, Island.class,
        Junktroller.class, PalisadeGiant.class, SnappingDrake.class, ViashinoFangtail.class})
class BrightflameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the target and color-sharing creatures, then gains the damage dealt")
    void damagesTargetAndColorSharingCreaturesAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Gains life only for damage that was actually dealt")
    void lifeGainUsesActualDamageAfterPrevention() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent firstAncestor = addCreatureReady(player1, new BenevolentAncestor());
        Permanent secondAncestor = addCreatureReady(player1, new BenevolentAncestor());
        activateAndResolve(firstAncestor, protectedCreature.getId());
        activateAndResolve(secondAncestor, protectedCreature.getId());

        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Damages creatures sharing either color with a multicolored target")
    void damagesCreaturesSharingEitherColorWithMulticoloredTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(redCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(whiteCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(differentColorCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A colorless target does not cause other colorless creatures to take damage")
    void colorlessTargetOnlyDamagesItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Junktroller());
        Permanent otherColorless = harness.addToBattlefieldAndReturn(player1, new Junktroller());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(otherColorless.getMarkedDamage()).isZero();
        assertThat(redCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Color sharing is determined against the target, without chaining through other creatures")
    void doesNotChainColorSharing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent multicoloredCreature = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(multicoloredCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(whiteCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("X equal to zero deals no damage and gains no life")
    void zeroXDealsNoDamageAndGainsNoLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life gain counts damage beyond the creatures' toughness")
    void gainsLifeForDamageBeyondToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        harness.addToBattlefield(player1, new BorosSwiftblade());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 5, target.getId());

        harness.assertInGraveyard(player2, "Viashino Fangtail");
        harness.assertInGraveyard(player1, "Boros Swiftblade");
        harness.assertLife(player1, 30);
    }

    @Test
    @CardUsed({PalisadeGiant.class})
    @DisplayName("Life gain includes damage redirected to another creature")
    void gainsLifeForDamageRedirectedToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoFangtail());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        harness.setHand(player1, List.of(new Brightflame()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 22);
    }

    private void activateAndResolve(Permanent source, UUID targetId) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, targetId);
        harness.passBothPriorities();
    }
}
