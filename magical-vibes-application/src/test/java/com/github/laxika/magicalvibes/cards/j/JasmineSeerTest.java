package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JasmineSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each white card in hand")
    void gainsLifeForWhiteCardsInHand() {
        Permanent seer = addReadySeer();
        harness.setHand(player1, List.of(new WhiteKnight(), new SerraAngel(), new Shock()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores nonwhite cards in hand")
    void ignoresNonwhiteCards() {
        addReadySeer();
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySeer();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new JasmineSeer());
        seer.setSummoningSick(false);
        return seer;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
