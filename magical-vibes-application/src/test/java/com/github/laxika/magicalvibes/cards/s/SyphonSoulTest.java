package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SyphonSoul.class)
class SyphonSoulTest extends BaseCardTest {

    private void castSyphonSoul() {
        harness.castFromHand(player1, new SyphonSoul(), "{2}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Syphon Soul deals 2 damage to the other player")
    void dealsDamageToOtherPlayer() {
        harness.setLife(player2, 20);

        castSyphonSoul();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Syphon Soul gains its controller life equal to the damage dealt")
    void gainsLifeEqualToDamage() {
        harness.setLife(player1, 20);

        castSyphonSoul();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @CardUsed(CircleOfProtectionBlack.class)
    @DisplayName("Syphon Soul only gains life for damage that was actually dealt")
    void onlyGainsLifeForDamageActuallyDealt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent circle = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());
        SyphonSoul syphonSoul = new SyphonSoul();

        harness.castFromHand(player1, syphonSoul, "{2}{B}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int circleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(circle);
        harness.activateAbility(player2, circleIndex, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, syphonSoul.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Syphon Soul goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castSyphonSoul();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Syphon Soul");
    }
}
