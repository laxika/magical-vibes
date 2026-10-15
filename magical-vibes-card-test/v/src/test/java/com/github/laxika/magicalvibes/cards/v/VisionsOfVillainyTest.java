package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HydraTroopers;
import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfVillainy.class, HydraTroopers.class, HydraulicHelper.class})
class VisionsOfVillainyTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less to cast while controlling a Villain")
    void costsOneLessWithVillain() {
        harness.addToBattlefield(player1, new HydraTroopers());
        harness.castFromHand(player1, new VisionsOfVillainy(), "{1}{B}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the cost reduction without controlling a Villain")
    void doesNotReduceCostWithoutVillain() {
        harness.setHand(player1, List.of(new VisionsOfVillainy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Draws two cards and loses 2 life")
    void drawsTwoCardsAndLosesTwoLife() {
        harness.setLibrary(player1, List.of(new HydraulicHelper(), new HydraulicHelper()));
        harness.castFromHand(player1, new VisionsOfVillainy(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void opponentsVillainDoesNotReduceCost() {
        harness.addToBattlefield(player2, new HydraTroopers());
        harness.setHand(player1, List.of(new VisionsOfVillainy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonVillainCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player1, new HydraulicHelper());
        harness.setHand(player1, List.of(new VisionsOfVillainy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void villainsOutsideBattlefieldDoNotReduceCost() {
        harness.setHand(player1, List.of(new VisionsOfVillainy(), new HydraTroopers()));
        harness.setGraveyard(player1, List.of(new HydraTroopers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleVillainsStillReduceCostByOnlyOne() {
        harness.addToBattlefield(player1, new HydraTroopers());
        harness.addToBattlefield(player1, new HydraTroopers());
        harness.castFromHand(player1, new VisionsOfVillainy(), "{2}{B}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionDoesNotRemoveBlackManaRequirement() {
        harness.addToBattlefield(player1, new HydraTroopers());
        harness.setHand(player1, List.of(new VisionsOfVillainy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void discountedSpellStillDrawsTwoAndLosesTwoLife() {
        harness.addToBattlefield(player1, new HydraTroopers());
        harness.setLibrary(player1, List.of(new HydraulicHelper(), new HydraulicHelper()));
        harness.castFromHand(player1, new VisionsOfVillainy(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
