package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.cards.w.WalkerOfTheWastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvelynTheCovetous.class, VampireOfTheDireMoon.class, GrizzlyBears.class, Mountain.class,
        WalkerOfTheWastes.class})
class EvelynTheCovetousTest extends BaseCardTest {

    @Test
    @DisplayName("A Vampire entering exiles the top card of each player's library with collection counters")
    void vampireEntryExilesEachLibraryTopCard() {
        Permanent evelyn = harness.addToBattlefieldAndReturn(player1, new EvelynTheCovetous());
        Card playerTop = new GrizzlyBears();
        Card opponentTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(playerTop));
        harness.setLibrary(player2, List.of(opponentTop));

        castVampireAndResolveTrigger();

        assertThat(gd.exiledCardsWithCollectionCounters)
                .containsExactlyInAnyOrder(playerTop.getId(), opponentTop.getId());
        assertThat(gd.findExiledCard(playerTop.getId()).exilerId()).isEqualTo(player1.getId());
        assertThat(gd.findExiledCard(opponentTop.getId()).exilerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(evelyn);
    }

    @Test
    @DisplayName("Evelyn lets you play one collection-counter card with mana of any color each turn")
    void playsOneCollectionCounterCardWithAnyColorManaEachTurn() {
        Permanent evelyn = harness.addToBattlefieldAndReturn(player1, new EvelynTheCovetous());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));

        castVampireAndResolveTrigger();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 2);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(evelyn);
    }

    @Test
    @DisplayName("A new Evelyn can play an earlier collection-counter card, but not one exiled by an opponent's ability")
    void newEvelynUsesControllerCollectionAndRejectsOpponentCollection() {
        Permanent firstEvelyn = harness.addToBattlefieldAndReturn(player1, new EvelynTheCovetous());
        Card collected = new GrizzlyBears();
        harness.setLibrary(player1, List.of(collected));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castVampireAndResolveTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(firstEvelyn);
        harness.addToBattlefield(player1, new EvelynTheCovetous());

        Card opponentCollected = new GrizzlyBears();
        gd.addToExileWithCollectionCounter(player2.getId(), opponentCollected, player2.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, opponentCollected.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, collected.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void ownEntryCollectsCardsAndEmptyLibrariesAreSkipped() {
        Card top = new Mountain();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new EvelynTheCovetous());
        harness.passBothPriorities();

        assertThat(gd.exiledCardsWithCollectionCounters).containsExactly(top.getId());
        assertThat(gd.findExiledCard(top.getId()).exilerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void nonVampireAndOpponentsVampireDoNotCollectCards() {
        harness.addToBattlefield(player1, new EvelynTheCovetous());
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new VampireOfTheDireMoon());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardsWithCollectionCounters).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void playingCollectedLandConsumesPermissionForTheTurn() {
        harness.addToBattlefield(player1, new EvelynTheCovetous());
        Card land = new Mountain();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(creature));
        castVampireAndResolveTrigger();

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Mountain");
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void collectedCardsCannotBePlayedWithoutEvelyn() {
        Permanent evelyn = harness.addToBattlefieldAndReturn(player1, new EvelynTheCovetous());
        Card collected = new GrizzlyBears();
        harness.setLibrary(player1, List.of(collected));
        harness.setLibrary(player2, List.of());
        castVampireAndResolveTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(evelyn);

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, collected.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(collected.getId())).isNotNull();
    }

    @Test
    void permissionResetsOnANewTurn() {
        harness.addToBattlefield(player1, new EvelynTheCovetous());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(second, new Mountain(), new Mountain()));
        castVampireAndResolveTrigger();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")).hasSize(2);
    }

    @Test
    void collectionPermissionDoesNotOverrideCreatureTiming() {
        harness.addToBattlefield(player1, new EvelynTheCovetous());
        Card collected = new GrizzlyBears();
        harness.setLibrary(player1, List.of(collected));
        harness.setLibrary(player2, List.of());
        castVampireAndResolveTrigger();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, collected.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, collected.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void anyColorPermissionCannotPayAColorlessManaRequirement() {
        harness.addToBattlefield(player1, new EvelynTheCovetous());
        Card collected = new WalkerOfTheWastes();
        harness.setLibrary(player1, List.of(collected));
        harness.setLibrary(player2, List.of());
        castVampireAndResolveTrigger();
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castFromExile(player1, collected.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(collected.getId())).isNotNull();
    }

    private void castVampireAndResolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VampireOfTheDireMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
