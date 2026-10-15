package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenOfEnduringHope;
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

@CardUsed({CatCollector.class, AvenOfEnduringHope.class})
class CatCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food artifact token")
    void entersWithFoodToken() {
        harness.setHand(player1, List.of(new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(food.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Food gains 3 life and creates a Cat")
    void sacrificingFoodGainsLifeAndCreatesCat() {
        harness.setHand(player1, List.of(new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one Cat for life gained the first time each turn")
    void createsOnlyOneCatPerTurn() {
        harness.addToBattlefield(player1, new CatCollector());
        harness.setHand(player1, List.of(new AvenOfEnduringHope(), new AvenOfEnduringHope()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    void foodOnOpponentsTurnDoesNotCreateCat() {
        harness.setHand(player1, List.of(new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isZero();
    }

    @Test
    void gainingLifeBeforeCollectorEntersPreventsTriggerOnLaterGain() {
        harness.setHand(player1, List.of(new AvenOfEnduringHope(), new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.assertLife(player1, 23);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isZero();
    }

    @Test
    void eachCollectorCreatesCatFromFirstLifeGain() {
        harness.addToBattlefield(player1, new CatCollector());
        harness.setHand(player1, List.of(new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
        Permanent cat = findPermanent(player1, "Cat");
        assertThat(cat.getCard().getPower()).isEqualTo(1);
        assertThat(cat.getCard().getToughness()).isEqualTo(1);
        assertThat(cat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(cat.getCard().getSubtypes()).contains(CardSubtype.CAT);
    }
}
