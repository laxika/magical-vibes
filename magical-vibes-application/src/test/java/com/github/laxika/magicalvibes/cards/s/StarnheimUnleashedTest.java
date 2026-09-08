package com.github.laxika.magicalvibes.cards.s;

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

class StarnheimUnleashedTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting creates one 4/4 Angel Warrior with flying and vigilance")
    void normalCastingCreatesOneAngelWarrior() {
        harness.setHand(player1, List.of(new StarnheimUnleashed()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertAngelWarriors(1);
    }

    @Test
    @DisplayName("Casting a foretold spell creates X Angel Warriors")
    void foretoldCastingCreatesXAngelWarriors() {
        StarnheimUnleashed card = new StarnheimUnleashed();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, card.getId(), 2, null);
        harness.passBothPriorities();

        assertAngelWarriors(2);
    }

    @Test
    @DisplayName("A foretold spell cast with X equal to zero creates no tokens")
    void foretoldCastingWithZeroCreatesNoTokens() {
        StarnheimUnleashed card = new StarnheimUnleashed();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, card.getId(), 0, null);
        harness.passBothPriorities();

        assertAngelWarriors(0);
    }

    private void assertAngelWarriors(int expectedCount) {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Angel Warrior"))
                .toList();

        assertThat(tokens).hasSize(expectedCount);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ANGEL, CardSubtype.WARRIOR);
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
            assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
        }
    }
}
