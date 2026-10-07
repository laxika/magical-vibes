package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TizerusCharger.class)
class TizerusChargerTest extends BaseCardTest {

    @Test
    void castingFromHandDoesNotOfferCounters() {
        harness.castFromHand(player1, new TizerusCharger(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        Permanent charger = findPermanent(player1, "Tizerus Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(charger.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void escapesWithPlusOnePlusOneCounterWhenChosen() {
        prepareEscape();
        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        harness.passBothPriorities();
        assertCounterChoice();
        harness.handleListChoice(player1, "+1/+1");

        Permanent charger = findPermanent(player1, "Tizerus Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(charger.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void escapeExilesFiveOtherCardsBeforeEntering() {
        List<Card> graveyard = prepareEscape();
        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard.subList(1, 6));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();

        harness.passBothPriorities();
        assertCounterChoice();
        harness.handleListChoice(player1, "flying");

        Permanent charger = findPermanent(player1, "Tizerus Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(charger.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void escapeCannotExileTheSpellItself() {
        List<Card> graveyard = prepareEscape();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeRequiresFiveDistinctOtherCards() {
        List<Card> graveyard = prepareEscape();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeCannotBePaidWithTheNormalManaCost() {
        prepareEscape();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private List<Card> prepareEscape() {
        List<Card> graveyard = List.of(new TizerusCharger(), new TizerusCharger(), new TizerusCharger(),
                new TizerusCharger(), new TizerusCharger(), new TizerusCharger());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        return graveyard;
    }

    private void assertCounterChoice() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("+1/+1", "flying");
    }
}
