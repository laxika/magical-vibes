package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DragonMage;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClawsOfWirewood.class, AvenFarseer.class, CoastWatcher.class, DragonMage.class,
        GoblinBrigand.class})
class ClawsOfWirewoodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each flying creature and each player")
    void damagesFlyersAndPlayers() {
        Permanent ownDragonMage = harness.addToBattlefieldAndReturn(player1, new DragonMage());
        Permanent opponentDragonMage = harness.addToBattlefieldAndReturn(player2, new DragonMage());
        harness.addToBattlefield(player2, new GoblinBrigand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ClawsOfWirewood(), "{3}{G}");
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(ownDragonMage.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentDragonMage.getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player2, "Goblin Brigand").getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kills a small flying creature")
    void killsSmallFlyer() {
        harness.addToBattlefield(player2, new AvenFarseer());

        harness.castFromHand(player1, new ClawsOfWirewood(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Farseer");
        harness.assertInGraveyard(player2, "Aven Farseer");
    }

    @Test
    @DisplayName("Protection from green prevents damage to a flying creature")
    void protectionFromGreenPreventsCreatureDamage() {
        Permanent protectedFlyer = harness.addToBattlefieldAndReturn(player2, new CoastWatcher());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ClawsOfWirewood(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(protectedFlyer.getMarkedDamage()).isEqualTo(0);
        harness.assertOnBattlefield(player2, "Coast Watcher");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ClawsOfWirewood()));
        harness.setLibrary(player1, List.of(new GoblinBrigand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Claws of Wirewood");
        harness.assertInHand(player1, "Goblin Brigand");
    }
}
