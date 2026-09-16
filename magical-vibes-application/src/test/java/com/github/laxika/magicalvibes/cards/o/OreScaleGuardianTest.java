package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OreScaleGuardian.class, Forest.class, Shock.class})
class OreScaleGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with no land cards in the controller's graveyard")
    void canBeCastForFullCost() {
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for each land card in the controller's graveyard")
    void costIsReducedForLandCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonland cards and lands in an opponent's graveyard do not reduce the cost")
    void ignoresNonlandAndOpponentGraveyardCards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The reduction cannot reduce the colored mana requirement")
    void reductionCannotReduceColoredManaRequirement() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new OreScaleGuardian()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
