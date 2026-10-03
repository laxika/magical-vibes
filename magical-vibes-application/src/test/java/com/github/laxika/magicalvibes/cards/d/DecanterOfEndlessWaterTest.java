package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecanterOfEndlessWater.class, GrizzlyBears.class})
class DecanterOfEndlessWaterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives its controller no maximum hand size")
    void givesControllerNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new DecanterOfEndlessWater());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent decanter = harness.addToBattlefieldAndReturn(player1, new DecanterOfEndlessWater());
        decanter.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(decanter.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Can produce each color immediately without using the stack")
    void producesEachColorOnEntry(ManaColor color) {
        Permanent decanter = harness.addToBattlefieldAndReturn(player1, new DecanterOfEndlessWater());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(decanter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(poolColor)).isZero();
        }
    }

    @Test
    @DisplayName("Does not remove the opponent's maximum hand size")
    void opponentStillDiscardsToSeven() {
        harness.addToBattlefield(player1, new DecanterOfEndlessWater());
        harness.setHand(player2, new ArrayList<>(List.of(
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater()
        )));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.getGameService().advanceStep(gd);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Maximum hand size returns after Decanter leaves the battlefield")
    void handLimitReturnsAfterLeavingBattlefield() {
        DecanterOfEndlessWater decanter = new DecanterOfEndlessWater();
        harness.addToBattlefield(player1, decanter);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(decanter));
        harness.setHand(player1, new ArrayList<>(List.of(
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater(),
                new DecanterOfEndlessWater(), new DecanterOfEndlessWater()
        )));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.getGameService().advanceStep(gd);

        PendingInteraction.DiscardChoice choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }
}
