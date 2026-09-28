package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TizerusCharger.class, GrizzlyBears.class})
class TizerusChargerTest extends BaseCardTest {

    @Test
    void entersWithPlusOnePlusOneCounterWhenChosen() {
        Permanent charger = castAndChoose("+1/+1");

        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(charger.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void entersWithFlyingCounterWhenChosen() {
        Permanent charger = castAndChoose("flying");

        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(charger.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void escapeExilesFiveOtherCardsBeforeEntering() {
        TizerusCharger charger = new TizerusCharger();
        List<GrizzlyBears> otherCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(charger, otherCards.get(0), otherCards.get(1), otherCards.get(2),
                otherCards.get(3), otherCards.get(4)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "flying");

        Permanent escapedCharger = findPermanent(player1, "Tizerus Charger");
        assertThat(escapedCharger.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(escapedCharger.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new TizerusCharger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("+1/+1", "flying");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Tizerus Charger");
    }
}
