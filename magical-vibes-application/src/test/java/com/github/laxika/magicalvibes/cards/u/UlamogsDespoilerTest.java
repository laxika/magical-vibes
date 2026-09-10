package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsDespoiler.class, PathToExile.class})
class UlamogsDespoilerTest extends BaseCardTest {

    @Test
    void mayPutTwoOpponentOwnedExiledCardsIntoGraveyardsAndEnterWithCounters() {
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(despoiler.getEffectivePower()).isEqualTo(9);
        assertThat(despoiler.getEffectiveToughness()).isEqualTo(9);
    }

    @Test
    void mayDeclineWithoutMovingCardsOrAddingCounters() {
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        castUlamogsDespoiler();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPromptWithoutTwoEligibleOpponentOwnedCards() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castUlamogsDespoiler() {
        harness.setHand(player1, List.of(new UlamogsDespoiler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
