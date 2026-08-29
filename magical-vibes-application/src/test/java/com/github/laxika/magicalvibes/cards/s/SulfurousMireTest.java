package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SulfurousMireTest extends BaseCardTest {

    @Test
    @DisplayName("Sulfurous Mire enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SulfurousMire()));
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);

        assertThat(findSulfurousMire(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sulfurous Mire produces the chosen black or red mana")
    void producesChosenMana() {
        for (ManaColor color : List.of(ManaColor.BLACK, ManaColor.RED)) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent mire = addSulfurousMireReady(player1);
            GameData gameData = harness.getGameData();

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gameData.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(mire.isTapped()).isTrue();
            assertThat(gameData.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addSulfurousMireReady(Player player) {
        Permanent mire = new Permanent(new SulfurousMire());
        mire.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(mire);
        return mire;
    }

    private Permanent findSulfurousMire(Player player) {
        return findPermanent(player, "Sulfurous Mire");
    }
}
