package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonMegaliths.class, NessianCourser.class})
class KeldonMegalithsTest extends BaseCardTest {

    @Test
    @DisplayName("Keldon Megaliths enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new KeldonMegaliths()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Keldon Megaliths").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one red mana")
    void tappingAddsRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KeldonMegaliths());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hellbent ability deals 1 damage to a player")
    void hellbentAbilityDealsDamageToPlayer() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KeldonMegaliths());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hellbent ability deals 1 damage to a creature")
    void hellbentAbilityDealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KeldonMegaliths());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hellbent ability cannot target a noncreature permanent")
    void hellbentAbilityCannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KeldonMegaliths());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KeldonMegaliths());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Hellbent ability cannot be activated with cards in hand")
    void hellbentAbilityRequiresEmptyHand() {
        harness.addToBattlefield(player1, new KeldonMegaliths());
        harness.setHand(player1, List.of(new KeldonMegaliths()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0 or fewer cards in your hand");
    }
}
