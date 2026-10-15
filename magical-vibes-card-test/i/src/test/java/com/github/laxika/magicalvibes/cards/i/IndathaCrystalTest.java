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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        return addCreatureReady(player1, new IndathaCrystal());
    }

    @Test
    @DisplayName("A newly controlled noncreature Crystal can produce mana immediately without using the stack")
    void newlyControlledCrystalProducesManaImmediately() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new IndathaCrystal());
        crystal.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(crystal.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Crystal cannot activate its mana ability")
    void tappedCrystalCannotProduceMana() {
        Permanent crystal = addReadyCrystal();
        crystal.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cycling pays two generic mana and discards before the draw resolves")
    void cyclingPaysCostsBeforeDrawing() {
        IndathaCrystal cycled = new IndathaCrystal();
        IndathaCrystal drawn = new IndathaCrystal();
        harness.setHand(player1, List.of(cycled));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot discard the Crystal when only one mana is available")
    void cyclingRequiresTwoMana() {
        IndathaCrystal crystal = new IndathaCrystal();
        harness.setHand(player1, List.of(crystal));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(crystal);
        harness.assertNotInGraveyard(player1, "Indatha Crystal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
