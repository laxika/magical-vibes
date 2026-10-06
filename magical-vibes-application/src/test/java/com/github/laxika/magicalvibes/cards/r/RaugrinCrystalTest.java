package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaugrinCrystal.class, GrizzlyBears.class})
class RaugrinCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of a chosen Raugrin Crystal color")
    void addsChosenManaColor() {
        harness.addToBattlefield(player1, new RaugrinCrystal());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling Raugrin Crystal discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RaugrinCrystal()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raugrin Crystal");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED", "WHITE"})
    void producesExactlyOneManaImmediatelyAndPaysTapCost(ManaColor color) {
        harness.addToBattlefield(player1, new RaugrinCrystal());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(findPermanent(player1, "Raugrin Crystal").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cyclingPaysGenericManaAndDiscardsBeforeDrawing() {
        RaugrinCrystal cycledCard = new RaugrinCrystal();
        RaugrinCrystal drawnCard = new RaugrinCrystal();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Raugrin Crystal");
        harness.assertInGraveyard(player1, "Raugrin Crystal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
    }

    @Test
    void cyclingWithInsufficientManaKeepsCardInHand() {
        harness.setHand(player1, List.of(new RaugrinCrystal()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Raugrin Crystal");
        harness.assertNotInGraveyard(player1, "Raugrin Crystal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
