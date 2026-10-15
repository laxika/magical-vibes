package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZagothTriome.class, GrizzlyBears.class})
class ZagothTriomeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ZagothTriome()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds one mana of a chosen Zagoth Triome color")
    void addsChosenManaColor() {
        harness.addToBattlefield(player1, new ZagothTriome());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ZagothTriome()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zagoth Triome");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "GREEN", "BLUE"})
    @DisplayName("Each mana choice adds exactly one mana immediately and taps the land")
    void producesEachColorWithoutUsingTheStack(ManaColor color) {
        var land = harness.addToBattlefieldAndReturn(player1, new ZagothTriome());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("A tapped Triome cannot activate its mana ability")
    void tappedLandCannotProduceMana() {
        harness.enterBattlefieldAndReturn(player1, new ZagothTriome());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires three mana and leaves the card in hand when payment fails")
    void cannotCycleWithOnlyTwoMana() {
        harness.setHand(player1, List.of(new ZagothTriome()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Zagoth Triome");
        harness.assertNotInGraveyard(player1, "Zagoth Triome");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling pays and discards immediately but draws only when the ability resolves")
    void cyclingCostsPrecedeTheDraw() {
        var cycledCard = new ZagothTriome();
        var drawnCard = new ZagothTriome();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
