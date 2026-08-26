package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenarianGlimmer.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class})
class VenarianGlimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a nonland card with mana value at most X and makes its player discard it")
    void choosesMatchingCardAndDiscardsIt() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card airElemental = new AirElemental();
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, new ArrayList<>(List.of(forest, shock, bears, airElemental)));
        addManaForX(2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Shock", "Air Elemental");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(new VenarianGlimmer(), new Shock())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
