package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BomatCourier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaeliaTheBladeReforged.class, BomatCourier.class, GrizzlyBears.class,
        TormodsCrypt.class, SwordsToPlowshares.class})
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

    @Test
    void graveyardExileTriggersOnOpponentsTurnEvenWhenOpponentExilesIt() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        Card graveyardCard = new LaeliaTheBladeReforged();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void simultaneousGraveyardExileAddsOnlyOneCounter() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        Card firstCard = new LaeliaTheBladeReforged();
        Card secondCard = new LaeliaTheBladeReforged();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(firstCard, secondCard);
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exilingOpponentsGraveyardDoesNotAddCounter() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        Card graveyardCard = new LaeliaTheBladeReforged();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exilingCreatureFromBattlefieldDoesNotAddCounter() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, courier.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(courier.getCard());
        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingWithEmptyLibraryDoesNotAddCounter() {
        Permanent laelia = addCreatureReady(player1, new LaeliaTheBladeReforged());
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureCanBeCastWithItsNormalManaCostAfterCombat() {
        addCreatureReady(player1, new LaeliaTheBladeReforged());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().getId()).isEqualTo(topCard.getId());
    }
}
