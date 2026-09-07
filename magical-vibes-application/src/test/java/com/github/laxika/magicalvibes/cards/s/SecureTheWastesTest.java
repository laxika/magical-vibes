package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SecureTheWastes.class)
class SecureTheWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 white Warrior tokens")
    void createsXWarriorTokens() {
        harness.setHand(player1, List.of(new SecureTheWastes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        });
    }

    @Test
    @DisplayName("X=0 creates no tokens")
    void zeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new SecureTheWastes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
