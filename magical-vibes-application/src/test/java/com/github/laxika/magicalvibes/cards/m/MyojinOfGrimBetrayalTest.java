package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfGrimBetrayal.class, GrizzlyBears.class, Shock.class})
class MyojinOfGrimBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with an indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyojinOfGrimBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Grim Betrayal");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get an indestructible counter")
    void enteringWithoutCastingDoesNotGetIndestructibleCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfGrimBetrayal());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the counter returns qualifying creatures from all graveyards")
    void returnsQualifyingCreaturesFromAllGraveyards() {
        Permanent myojin = addReadyMyojin(player1);
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        Card oldCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        harness.setGraveyard(player1, List.of(ownCreature, oldCreature, nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player1.getId(), new HashSet<>(List.of(ownCreature.getId(), nonCreature.getId())));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player2.getId(), new HashSet<>(List.of(opposingCreature.getId())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(oldCreature.getId(), nonCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without an indestructible counter")
    void cannotActivateWithoutIndestructibleCounter() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = harness.addToBattlefieldAndReturn(player, new MyojinOfGrimBetrayal());
        myojin.setSummoningSick(false);
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        return myojin;
    }
}
