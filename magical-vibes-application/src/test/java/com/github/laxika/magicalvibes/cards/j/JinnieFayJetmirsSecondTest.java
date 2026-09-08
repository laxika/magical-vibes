package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JinnieFayJetmirsSecond.class, RaiseTheAlarm.class})
class JinnieFayJetmirsSecondTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a token creation event with Cat tokens")
    void createsCats() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cat");

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(cat.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(cat.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
            assertThat(cat.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(cat.getEffectivePower()).isEqualTo(2);
            assertThat(cat.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Replaces a token creation event with Dog tokens")
    void createsDogs() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dog");

        List<Permanent> dogs = findPermanents(player1, "Dog");
        assertThat(dogs).hasSize(2);
        assertThat(dogs).allSatisfy(dog -> {
            assertThat(dog.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(dog.getCard().getSubtypes()).containsExactly(CardSubtype.DOG);
            assertThat(dog.getCard().getKeywords()).contains(Keyword.VIGILANCE);
            assertThat(dog.getEffectivePower()).isEqualTo(3);
            assertThat(dog.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("May keep the original tokens")
    void keepsOriginalTokensWhenReplacementIsDeclined() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Original tokens");

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }
}
