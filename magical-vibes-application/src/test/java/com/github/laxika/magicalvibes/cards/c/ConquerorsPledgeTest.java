package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConquerorsPledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker creates six 1/1 white Kor Soldier tokens")
    void castWithoutKickerCreatesSixTokens() {
        harness.setHand(player1, List.of(new ConquerorsPledge()));
        addBaseMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertKorSoldiers(6);
    }

    @Test
    @DisplayName("Cast with kicker creates twelve 1/1 white Kor Soldier tokens")
    void castWithKickerCreatesTwelveTokens() {
        harness.setHand(player1, List.of(new ConquerorsPledge()));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertKorSoldiers(12);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void assertKorSoldiers(int expectedCount) {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Kor Soldier"))
                .toList();

        assertThat(tokens).hasSize(expectedCount);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KOR, CardSubtype.SOLDIER);
        }
    }
}
