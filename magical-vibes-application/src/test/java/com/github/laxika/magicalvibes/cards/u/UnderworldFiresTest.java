package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldFires.class, FugitiveWizard.class, GrizzlyBears.class, ChandraNalaar.class,
        ChoMannoRevolutionary.class, WrathOfGod.class})
class UnderworldFiresTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature and exiles creatures killed by it")
    void damagesCreaturesAndExilesThoseKilled() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castUnderworldFires();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player2, "Fugitive Wizard");
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fugitive Wizard"));
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Exiles planeswalkers reduced to zero loyalty by its damage")
    void exilesPlaneswalkersReducedToZeroLoyalty() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 1);

        castUnderworldFires();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertNotInGraveyard(player2, "Chandra Nalaar");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Chandra Nalaar"));
    }

    @Test
    @DisplayName("Marks surviving creatures dealt damage for exile if they die later this turn")
    void marksSurvivingCreaturesForLaterExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castUnderworldFires();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.isExileInsteadOfDieThisTurn()).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castUnderworldFires();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiles a damaged planeswalker that dies to another source later this turn")
    void exilesPlaneswalkerKilledLaterThisTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        source.setCounterCount(CounterType.LOYALTY, 6);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        victim.setCounterCount(CounterType.LOYALTY, 2);

        castUnderworldFires();

        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        assertThat(victim.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.activateAbility(player1, 0, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertNotInGraveyard(player2, "Chandra Nalaar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Chandra Nalaar"));
    }

    @Test
    @DisplayName("Exiles surviving damaged creatures destroyed later this turn")
    void exilesCreatureDestroyedLaterThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castUnderworldFires();
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not exile a creature whose damage was entirely prevented")
    void doesNotExileCreatureWithPreventedDamage() {
        Permanent choManno = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castUnderworldFires();

        assertThat(choManno.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cho-Manno, Revolutionary");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Cho-Manno, Revolutionary"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The exile replacement expires when the turn ends")
    void doesNotExileCreatureDestroyedNextTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new FugitiveWizard()));

        castUnderworldFires();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void castUnderworldFires() {
        harness.castFromHand(player1, new UnderworldFires(), "{1}{R}");
        harness.passBothPriorities();
    }
}
