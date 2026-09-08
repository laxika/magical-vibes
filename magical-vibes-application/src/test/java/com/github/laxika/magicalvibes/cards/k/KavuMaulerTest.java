package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KavuMauler.class)
class KavuMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other attacking Kavu")
    void boostsForOtherAttackingKavus() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, createKavuCard("Kavu A"));
        addCreatureReady(player1, createKavuCard("Kavu B"));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a bonus when attacking alone")
    void noBonusWhenAttackingAlone() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only other attacking Kavus")
    void ignoresNonKavuAndNonAttackingCreatures() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, createKavuCard("Attacking Kavu"));
        addCreatureReady(player1, createCard("Human Soldier", CardSubtype.HUMAN, CardSubtype.SOLDIER));
        addCreatureReady(player1, createKavuCard("Staying Home Kavu"));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isEqualTo(1);
        assertThat(mauler.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary bonus resets at end of turn")
    void bonusResetsAtEndOfTurn() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, createKavuCard("Kavu A"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(mauler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }

    private Card createKavuCard(String name) {
        return createCard(name, CardSubtype.KAVU);
    }

    private Card createCard(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtypes));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
