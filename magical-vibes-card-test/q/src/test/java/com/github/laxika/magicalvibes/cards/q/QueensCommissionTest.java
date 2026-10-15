package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueensCommission.class})
class QueensCommissionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 white Vampire creature tokens with lifelink")
    void createsTwoVampireTokensWithLifelink() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new QueensCommission(), "{2}{W}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(2);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new QueensCommission(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Queen's Commission");
    }

    @Test
    @DisplayName("Both Vampire tokens gain life for their controller through combat damage")
    void tokensGainLifeThroughCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new QueensCommission(), "{2}{W}");
        harness.passBothPriorities();

        findPermanents(player1, "Vampire").forEach(token -> token.setSummoningSick(false));
        declareAttackers(List.of(0, 1));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
