package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgScavengers.class, SerraAngel.class, DarksteelCitadel.class})
class UrborgScavengersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering exiles a target card, puts a counter on Urborg Scavengers, and grants its keyword")
    void enteringExilesCardPutsCounterAndGrantsKeyword() {
        Card card = new DarksteelCitadel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card)));
        harness.setHand(player1, List.of(new UrborgScavengers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        Permanent scavengers = findPermanent(player1, "Urborg Scavengers");
        assertThat(gd.getCardsExiledByPermanent(scavengers.getId())).containsExactly(card);
        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, scavengers).keywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Attacking exiles a target card and puts another counter on Urborg Scavengers")
    void attackingExilesCardAndPutsCounter() {
        Permanent scavengers = addCreatureReady(player1, new UrborgScavengers());
        Card card = new SerraAngel();
        harness.setGraveyard(player2, List.of(card));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(scavengers.getId())).containsExactly(card);
        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that leaves the graveyard does not produce the counter")
    void missingTargetDoesNotProduceCounter() {
        Card card = new SerraAngel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card)));
        harness.setHand(player1, List.of(new UrborgScavengers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        Permanent scavengers = findPermanent(player1, "Urborg Scavengers");
        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(scavengers.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combines the watched keywords from every card exiled with Urborg Scavengers")
    void combinesKeywordsFromAllExiledCards() {
        Permanent scavengers = addCreatureReady(player1, new UrborgScavengers());
        gd.addToExile(player1.getId(), new SerraAngel(), scavengers.getId());
        gd.addToExile(player1.getId(), new DarksteelCitadel(), scavengers.getId());

        assertThat(gqs.computeStaticBonus(gd, scavengers).keywords())
                .contains(Keyword.FLYING, Keyword.VIGILANCE, Keyword.INDESTRUCTIBLE)
                .doesNotContain(Keyword.DEFENDER);
    }
}
