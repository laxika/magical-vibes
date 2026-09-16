package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioticBeast.class, Starstorm.class})
class SymbioticBeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Symbiotic Beast dies, its controller creates four Insect tokens")
    void deathTriggerCreatesFourInsectTokens() {
        harness.addToBattlefield(player1, new SymbioticBeast());

        harness.setHand(player2, List.of(new Starstorm()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, 4, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Symbiotic Beast");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Insect");
    }

    @Test
    @DisplayName("Death trigger creates four 1/1 green Insect creature tokens")
    void deathTriggerCreatesCorrectInsectTokens() {
        harness.addToBattlefield(player1, new SymbioticBeast());

        harness.setHand(player2, List.of(new Starstorm()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, 4, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(4).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(insect.getCard().getPower()).isEqualTo(1);
            assertThat(insect.getCard().getToughness()).isEqualTo(1);
            assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        });
    }

    @Test
    @DisplayName("Death trigger creates tokens for the Beast's controller")
    void deathTriggerCreatesTokensForController() {
        harness.addToBattlefield(player2, new SymbioticBeast());

        harness.setHand(player1, List.of(new Starstorm()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Symbiotic Beast");
        assertThat(countPermanents(player2, "Insect")).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Insect");
    }
}
