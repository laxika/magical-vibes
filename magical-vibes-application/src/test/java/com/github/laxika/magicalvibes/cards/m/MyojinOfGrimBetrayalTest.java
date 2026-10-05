package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
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

@CardUsed({MyojinOfGrimBetrayal.class, GrizzlyBears.class, Shock.class, SoulWarden.class, GrafdiggersCage.class})
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

    @Test
    @DisplayName("Creatures returned together see each other's entry")
    void returnedCreaturesEnterSimultaneously() {
        addReadyMyojin(player1);
        Card bear = new GrizzlyBears();
        Card warden = new SoulWarden();
        harness.setGraveyard(player1, List.of(bear, warden));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player1.getId(), new HashSet<>(List.of(bear.getId(), warden.getId())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 1);
    }

    @Test
    @DisplayName("Grafdigger's Cage leaves blocked cards in their original graveyard")
    void blockedOpposingCreatureStaysInOwnersGraveyard() {
        addReadyMyojin(player1);
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bear));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player2.getId(), new HashSet<>(List.of(bear.getId())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.cardsPutIntoGraveyardFromAnywhereThisTurn.get(player2.getId()))
                .contains(bear.getId());
    }

    @Test
    @DisplayName("A creature dying in response is returned even after Myojin dies")
    void includesCreaturesDyingInResponseAndResolvesWithoutSource() {
        Permanent myojin = addReadyMyojin(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, myojin.getId());
        harness.assertInGraveyard(player1, "Myojin of Grim Betrayal");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Myojin of Grim Betrayal");
        assertThat(returned.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        harness.assertNotInGraveyard(player1, "Myojin of Grim Betrayal");
    }

    @Test
    @DisplayName("A tapped summoning-sick Myojin can activate with no eligible creatures")
    void tappedSummoningSickSourceCanActivateWithEmptyGraveyards() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setSummoningSick(true);
        myojin.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Myojin of Grim Betrayal");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = harness.addToBattlefieldAndReturn(player, new MyojinOfGrimBetrayal());
        myojin.setSummoningSick(false);
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        return myojin;
    }
}
