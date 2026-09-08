package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndathaCrystal.class, GrizzlyBears.class})
class IndathaCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Indatha Crystal prompts for white, black, or green mana")
    void tappingPromptsForColor() {
        addReadyCrystal();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "GREEN");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLACK", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();
            addReadyCrystal();

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color)))
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Cycling Indatha Crystal discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IndathaCrystal()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Indatha Crystal");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyCrystal() {
        Permanent crystal = new Permanent(new IndathaCrystal());
        crystal.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crystal);
        return crystal;
    }
}
