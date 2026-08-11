package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarduHordechiefTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a 1/1 white Warrior token when Mardu Hordechief enters")
    void raidCreatesWarriorToken() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castMarduHordechief();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Raid does not create a token when Mardu Hordechief enters without attacking")
    void noRaidDoesNotCreateWarriorToken() {
        castMarduHordechief();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        harness.assertOnBattlefield(player1, "Mardu Hordechief");
    }

    private void castMarduHordechief() {
        harness.setHand(player1, List.of(new MarduHordechief()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }
}
