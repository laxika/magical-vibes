package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VolatileFjordTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new VolatileFjord()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Volatile Fjord").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingForRedMana() {
        tapFor(ManaColor.RED);
    }

    private void tapFor(ManaColor color) {
        Permanent land = addLandReady(player1);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gameData.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addLandReady(Player player) {
        Permanent permanent = new Permanent(new VolatileFjord());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
