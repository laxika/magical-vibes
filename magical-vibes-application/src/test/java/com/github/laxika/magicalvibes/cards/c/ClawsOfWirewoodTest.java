package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClawsOfWirewood.class, AirElemental.class, GrizzlyBears.class, SuntailHawk.class})
class ClawsOfWirewoodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each flying creature and each player")
    void damagesFlyersAndPlayers() {
        Permanent ownAirElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentAirElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClawsOfWirewood()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(ownAirElemental.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentAirElemental.getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kills a small flying creature")
    void killsSmallFlyer() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new ClawsOfWirewood()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ClawsOfWirewood()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Claws of Wirewood");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
