package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightsquadCommando.class})
class NightsquadCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a 1/1 white Human Soldier token when Nightsquad Commando enters")
    void raidCreatesHumanSoldierToken() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castCommando();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Raid does not create a token when you did not attack this turn")
    void noRaidDoesNotCreateToken() {
        castCommando();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    private void castCommando() {
        harness.setHand(player1, List.of(new NightsquadCommando()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
