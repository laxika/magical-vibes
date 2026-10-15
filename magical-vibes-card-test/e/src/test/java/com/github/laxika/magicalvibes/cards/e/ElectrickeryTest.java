package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BellowsLizard;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Electrickery.class, DrudgeBeetle.class, BellowsLizard.class, RubblebackRhino.class})
class ElectrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature you don't control")
    void damagesTargetCreature() {
        Permanent target = addCreature(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = addCreature(player1);
        addCreature(player2);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Overloaded, it damages every creature you don't control and needs no target")
    void overloadDamagesEveryCreatureYouDontControl() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreature(player2);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting damages only the chosen opposing creature")
    void normalCastLeavesOtherOpposingCreaturesUndamaged() {
        Permanent target = addCreature(player2);
        Permanent other = addCreature(player2);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Normal casting cannot target an opposing hexproof creature")
    void normalCastCannotTargetHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload damages hexproof creatures and kills opposing one-toughness creatures")
    void overloadBypassesHexproofAndDealsLethalDamage() {
        Permanent hexproof = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.addToBattlefield(player2, new BellowsLizard());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BellowsLizard());
        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(hexproof.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Bellows Lizard");
        harness.assertInGraveyard(player2, "Bellows Lizard");
        harness.assertOnBattlefield(player1, "Bellows Lizard");
        assertThat(own.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife);
    }

    @Test
    @DisplayName("Overload can be cast with no opposing creatures")
    void overloadCanBeCastWithoutOpposingCreatures() {
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Electrickery");
    }

    @Test
    @DisplayName("Normal casting does not damage a target that comes under your control")
    void normalCastRechecksControlAtResolution() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Electrickery");
    }

    @Test
    @DisplayName("Overload determines affected creatures when it resolves")
    void overloadUsesCurrentControllersAndBattlefield() {
        Permanent formerlyOpposing = addCreature(player2);
        Permanent formerlyOwn = addCreature(player1);
        harness.setHand(player1, List.of(new Electrickery()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castWithOverload(player1, 0);

        gd.playerBattlefields.get(player2.getId()).remove(formerlyOpposing);
        gd.playerBattlefields.get(player1.getId()).add(formerlyOpposing);
        gd.playerBattlefields.get(player1.getId()).remove(formerlyOwn);
        gd.playerBattlefields.get(player2.getId()).add(formerlyOwn);
        Permanent newlyEntered = addCreature(player2);
        harness.passBothPriorities();

        assertThat(formerlyOpposing.getMarkedDamage()).isZero();
        assertThat(formerlyOwn.getMarkedDamage()).isEqualTo(1);
        assertThat(newlyEntered.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DrudgeBeetle());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
