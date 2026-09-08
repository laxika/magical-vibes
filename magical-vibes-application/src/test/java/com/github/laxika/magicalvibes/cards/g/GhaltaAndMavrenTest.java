package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GhaltaAndMavren.class)
class GhaltaAndMavrenTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur mode uses the greatest power among other attackers")
    void dinosaurModeUsesGreatestOtherAttackerPower() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, creature("Large attacker", 5, 5));
        addCreatureReady(player1, creature("Small attacker", 2, 2));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");

        Permanent dinosaur = findPermanents(player1, "Dinosaur").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(dinosaur.getCard().getPower()).isEqualTo(5);
        assertThat(dinosaur.getCard().getToughness()).isEqualTo(5);
        assertThat(dinosaur.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(dinosaur.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(dinosaur.isTapped()).isTrue();
        assertThat(dinosaur.isAttackedThisTurn()).isTrue();
        assertThat(countPermanents(player1, "Vampire")).isZero();
    }

    @Test
    @DisplayName("Vampire mode triggers when other creatures attack without Ghalta and Mavren")
    void vampireModeTriggersWithoutSourceAttacking() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, creature("Attacker one", 3, 3));
        addCreatureReady(player1, creature("Attacker two", 2, 2));
        addCreatureReady(player1, creature("Attacker three", 1, 1));

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");

        List<Permanent> vampires = findPermanents(player1, "Vampire").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(vampires).hasSize(3);
        assertThat(vampires).allSatisfy(vampire -> {
            assertThat(vampire.getCard().getPower()).isEqualTo(1);
            assertThat(vampire.getCard().getToughness()).isEqualTo(1);
            assertThat(vampire.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(vampire.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
            assertThat(vampire.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
        assertThat(findPermanents(player1, "Dinosaur").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of());
        return card;
    }
}
