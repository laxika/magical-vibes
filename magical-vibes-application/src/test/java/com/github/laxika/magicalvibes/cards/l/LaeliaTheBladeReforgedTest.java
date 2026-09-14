package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BomatCourier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaeliaTheBladeReforged.class, BomatCourier.class, GrizzlyBears.class})
class LaeliaTheBladeReforgedTest extends BaseCardTest {

    @Test
    void attackingExilesTopCardAndLetsYouPlayItThisTurn() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsAnotherCounterForEachExileEventDuringYourTurn() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        addCreatureReady(player1, new BomatCourier());
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(firstCard, secondCard);
    }
}
