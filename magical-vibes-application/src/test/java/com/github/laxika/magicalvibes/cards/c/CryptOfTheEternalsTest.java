package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptOfTheEternalsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 1 life")
    void etbGainsOneLife() {
        harness.setHand(player1, List.of(new CryptOfTheEternals()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapForColorless() {
        harness.addToBattlefield(player1, new CryptOfTheEternals());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T} prompts a choice between blue, black and red")
    void filterPromptsColorChoice() {
        harness.addToBattlefield(player1, new CryptOfTheEternals());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED");
    }

    @Test
    @DisplayName("{1}, {T} adds exactly one mana of the chosen color")
    void filterAddsChosenColor() {
        for (String color : new String[]{"BLUE", "BLACK", "RED"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new CryptOfTheEternals());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, color);

            var pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.get(manaColor)).isEqualTo(1);
            assertThat(pool.get(ManaColor.COLORLESS)).isZero();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Filter ability cannot be activated without {1} to pay")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new CryptOfTheEternals());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
