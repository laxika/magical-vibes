package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HighlandWealdTest extends BaseCardTest {

    @Test
    @DisplayName("Highland Weald enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new HighlandWeald()));
        harness.playLand(player1, 0);

        Permanent weald = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(weald.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Highland Weald adds the chosen red or green mana")
    void addsChosenMana() {
        for (String color : new String[]{"RED", "GREEN"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent weald = new Permanent(new HighlandWeald());
            weald.setSummoningSick(false);
            GameData gameData = harness.getGameData();
            gameData.playerBattlefields.get(player1.getId()).add(weald);

            harness.activateAbility(player1, 0, 0, null, null);

            PendingInteraction.ColorChoice choice = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice).isNotNull();
            harness.handleListChoice(player1, color);

            assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color))).isEqualTo(1);
            assertThat(weald.isTapped()).isTrue();
        }
    }
}
