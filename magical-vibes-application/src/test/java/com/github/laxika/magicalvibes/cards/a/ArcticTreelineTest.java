package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcticTreeline.class})
class ArcticTreelineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ArcticTreeline()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Arctic Treeline").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingForWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    @Test
    @DisplayName("Cannot activate the mana ability while the land is tapped")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new ArcticTreeline()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can produce mana after untapping a played land")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new ArcticTreeline()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(findPermanent(player1, "Arctic Treeline").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Both mana choices produce mana usable for snow costs")
    void bothColorsProduceSnowSourceMana() {
        Permanent land = addLandReady(player1);

        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.WHITE)) {
            land.untap();
            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(color)).isEqualTo(1);
        }
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ArcticTreeline());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
