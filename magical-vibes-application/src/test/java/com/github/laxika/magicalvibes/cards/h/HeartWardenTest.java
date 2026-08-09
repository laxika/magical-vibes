package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeartWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Heart Warden produces one green mana")
    void tappingProducesGreenMana() {
        Permanent warden = addReadyWarden(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Heart Warden draws a card")
    void sacrificeDrawsCard() {
        addReadyWarden(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gameData = harness.getGameData();
        int handSizeBefore = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Heart Warden");
        harness.assertInGraveyard(player1, "Heart Warden");
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyWarden(Player player) {
        Permanent warden = new Permanent(new HeartWarden());
        warden.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(warden);
        return warden;
    }
}
