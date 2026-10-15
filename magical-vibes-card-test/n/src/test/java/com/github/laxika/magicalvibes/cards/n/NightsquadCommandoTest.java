package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentsAttackDoesNotCreateToken() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        castCommando();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Human Soldier");
        harness.assertNotOnBattlefield(player2, "Human Soldier");
    }

    @Test
    @DisplayName("Raid trigger resolves after Nightsquad Commando leaves the battlefield")
    void raidTriggerResolvesWithoutSource() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castCommando();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Human Soldier");

        Permanent commando = findPermanent(player1, "Nightsquad Commando");
        gd.playerBattlefields.get(player1.getId()).remove(commando);
        gd.playerGraveyards.get(player1.getId()).add(commando.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Human Soldier");
        harness.assertNotOnBattlefield(player2, "Human Soldier");
    }

    private void castCommando() {
        harness.castFromHand(player1, new NightsquadCommando(), "{2}{B}");
    }
}
