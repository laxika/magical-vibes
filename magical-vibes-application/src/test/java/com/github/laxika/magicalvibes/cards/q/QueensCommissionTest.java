package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueensCommissionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 white Vampire creature tokens with lifelink")
    void createsTwoVampireTokensWithLifelink() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Vampire"))
                .toList();
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
        harness.setHand(player1, List.of(new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Queen's Commission"));
    }
}
