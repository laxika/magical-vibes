package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DreamstoneHedronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dreamstone Hedron adds three colorless mana")
    void tappingAddsThreeColorlessMana() {
        Permanent hedron = harness.addToBattlefieldAndReturn(player1, new DreamstoneHedron());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(hedron.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Dreamstone Hedron draws three cards")
    void sacrificingDrawsThreeCards() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Dreamstone Hedron");
        harness.assertInGraveyard(player1, "Dreamstone Hedron");

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }
}
