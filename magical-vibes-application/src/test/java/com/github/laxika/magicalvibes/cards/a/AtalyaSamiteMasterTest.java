package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Gains X life with the life mode")
    void gainsXLife() {
        harness.setLife(player1, 10);
        Permanent atalya = addCreatureReady(player1, new AtalyaSamiteMaster());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
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
    @DisplayName("The prevention mode cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new AtalyaSamiteMaster());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
