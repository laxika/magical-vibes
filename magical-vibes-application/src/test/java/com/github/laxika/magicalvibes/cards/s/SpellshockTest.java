package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellshockTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who casts a spell")
    void damagesOpponentCastingSpell() {
        harness.addToBattlefield(player1, new Spellshock());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals 2 damage to its controller when they cast a spell")
    void damagesControllerCastingSpell() {
        harness.addToBattlefield(player1, new Spellshock());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }
}
