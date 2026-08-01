package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreetSpasmTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target creature without flying you don't control")
    void damagesTargetCreature() {
        Permanent target = addPermanent(player2, new GrizzlyBears());
        Permanent own = addPermanent(player1, new GrizzlyBears());
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
        Permanent flyer = addPermanent(player2, new AirElemental());
        addPermanent(player2, new GrizzlyBears());
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
        Permanent own = addPermanent(player1, new GrizzlyBears());
        addPermanent(player2, new GrizzlyBears());
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
        Permanent first = addPermanent(player2, new GrizzlyBears());
        Permanent second = addPermanent(player2, new GrizzlyBears());
        Permanent flyer = addPermanent(player2, new AirElemental());
        Permanent own = addPermanent(player1, new GrizzlyBears());
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
        addPermanent(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StreetSpasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
