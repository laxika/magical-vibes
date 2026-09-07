package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldFires.class, FugitiveWizard.class, GrizzlyBears.class, ChandraNalaar.class})
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
    @DisplayName("Damages planeswalkers without exiling them for having zero loyalty")
    void damagesPlaneswalkersWithoutApplyingDeathReplacement() {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(chandra);

        castUnderworldFires();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Chandra Nalaar"));
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

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castUnderworldFires() {
        harness.setHand(player1, List.of(new UnderworldFires()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
