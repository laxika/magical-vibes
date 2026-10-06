package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScourTheLaboratory.class, Island.class, GrizzlyBears.class, Shock.class,
        Pacifism.class, Ornithopter.class})
class ScourTheLaboratoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Scour the Laboratory");
    }

    @Test
    @DisplayName("Delirium reduces the casting cost by {2}")
    void deliriumReducesCastingCost() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Pacifism(), new Ornithopter()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot cast for the reduced cost without delirium")
    void reducedCostRequiresDelirium() {
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("Three graveyard cards can provide four card types")
    void multiTypeCardCountsEachType() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Shock(), new Pacifism()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Four graveyard cards with only three card types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable delirium")
    void opponentGraveyardDoesNotReduceCost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player2, List.of(new Ornithopter(), new Shock(), new Pacifism()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("Delirium does not reduce the two blue mana requirement")
    void deliriumDoesNotReduceColoredMana() {
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Shock(), new Pacifism()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("Losing delirium after casting does not change the draw effect")
    void losingDeliriumAfterCastingStillDrawsThree() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Shock(), new Pacifism()));
        harness.setHand(player1, List.of(new ScourTheLaboratory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Scour the Laboratory");
    }
}
