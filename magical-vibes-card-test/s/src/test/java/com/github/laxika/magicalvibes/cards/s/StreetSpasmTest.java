package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreetSpasm.class, DrudgeBeetle.class, SunspireGriffin.class, RubblebackRhino.class})
class StreetSpasmTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target creature without flying you don't control")
    void damagesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SunspireGriffin());
        harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying you don't control");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying you don't control");
    }

    @Test
    @DisplayName("Overloaded, it deals X damage to each creature without flying you don't control")
    void overloadDamagesEveryNonFlyerYouDontControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SunspireGriffin());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(flyer.getMarkedDamage()).isZero();
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Overload pays X twice, so X=2 needs six mana")
    void overloadPaysXTwice() {
        harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload damages hexproof creatures because it does not target")
    void overloadDamagesHexproofCreature() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(rhino.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rhino);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Normal casting cannot target an opponent's hexproof creature")
    void cannotTargetHexproofCreature() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, rhino.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload can resolve with no creatures on the battlefield")
    void overloadNeedsNoTargets() {
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithOverload(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Street Spasm");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Normal casting with X zero deals no damage")
    void zeroXDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertInGraveyard(player1, "Street Spasm");
    }
}
