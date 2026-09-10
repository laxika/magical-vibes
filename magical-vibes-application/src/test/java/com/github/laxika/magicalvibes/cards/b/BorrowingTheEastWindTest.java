package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorrowingTheEastWind.class, ShuCavalry.class, ShuFootSoldiers.class})
class BorrowingTheEastWindTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to creatures with horsemanship")
    void damagesHorsemanshipCreatures() {
        harness.addToBattlefield(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new BorrowingTheEastWind()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Shu Cavalry (2/2) takes 2 damage and dies.
        harness.assertNotOnBattlefield(player2, "Shu Cavalry");
    }

    @Test
    @DisplayName("Deals X damage to horsemanship creatures controlled by either player")
    void damagesHorsemanshipCreaturesControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new ShuCavalry());
        harness.addToBattlefield(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new BorrowingTheEastWind()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player1, "Shu Cavalry");
        harness.assertNotOnBattlefield(player2, "Shu Cavalry");
    }

    @Test
    @DisplayName("Does not damage creatures without horsemanship")
    void doesNotDamageNonHorsemanshipCreatures() {
        harness.addToBattlefield(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new BorrowingTheEastWind()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Shu Foot Soldiers has no horsemanship, so it survives.
        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Deals X damage to each player")
    void damagesEachPlayer() {
        harness.setHand(player1, List.of(new BorrowingTheEastWind()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new BorrowingTheEastWind()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();

        harness.assertOnBattlefield(player2, "Shu Cavalry");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
