package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtalyaSamiteMaster.class, GrizzlyBears.class, Forest.class, Shock.class})
class AtalyaSamiteMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents X damage to a target creature")
    void preventsDamageToTargetCreature() {
        Permanent atalya = addCreatureReady(player1, new AtalyaSamiteMaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, 3, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        assertThat(atalya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents only the next X damage to the target creature")
    void preventsOnlyNextXDamageToTargetCreature() {
        addCreatureReady(player1, new AtalyaSamiteMaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, 3, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Gains X life with the life mode")
    void gainsXLife() {
        harness.setLife(player1, 10);
        Permanent atalya = addCreatureReady(player1, new AtalyaSamiteMaster());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(atalya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spending nonwhite mana on X is illegal")
    void onlyWhiteManaMayBeSpentOnX() {
        addCreatureReady(player1, new AtalyaSamiteMaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The life mode also requires white mana for X")
    void lifeModeOnlyAllowsWhiteManaForX() {
        addCreatureReady(player1, new AtalyaSamiteMaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The prevention mode cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new AtalyaSamiteMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
