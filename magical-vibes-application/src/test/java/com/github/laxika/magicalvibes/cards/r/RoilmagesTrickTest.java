package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilmagesTrick.class, GrizzlyBears.class})
class RoilmagesTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Gives opposing creatures -X/-0 and draws a card")
    void debuffsOpposingCreaturesAndDrawsCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, drawnCard);

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(ownBears.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBears.getEffectivePower()).isEqualTo(-1);
        assertThat(opposingBears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Counts each colored mana only once and excludes colorless mana")
    void countsDistinctColorsOnly() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("The penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(opposingBears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opposingBears.getEffectivePower()).isEqualTo(2);
    }
}
