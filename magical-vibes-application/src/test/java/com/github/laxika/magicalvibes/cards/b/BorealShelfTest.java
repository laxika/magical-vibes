package com.github.laxika.magicalvibes.cards.b;

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

class BorealShelfTest extends BaseCardTest {

    @Test
    @DisplayName("Boreal Shelf enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new BorealShelf()));
        harness.playLand(player1, 0);

        Permanent shelf = findPermanent(player1, "Boreal Shelf");

        assertThat(shelf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boreal Shelf adds the chosen white or blue mana")
    void addsChosenMana() {
        for (String color : new String[]{"WHITE", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());
            shelf.setSummoningSick(false);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);

            PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice).isNotNull();
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(shelf.isTapped()).isTrue();
        }
    }
}
