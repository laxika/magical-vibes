package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wall Off")
@CardUsed({WallOff.class, GrizzlyBears.class})
class WallOffTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each opposing creature, but not for your own creatures")
    void costReductionCountsOpposingCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WallOff()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without enough mana after opposing-creature reduction")
    void requiresRemainingMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WallOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creates a 0/4 colorless Wall token with defender and gains 4 life")
    void createsWallAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WallOff()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wall.getCard().getPower()).isEqualTo(0);
        assertThat(wall.getCard().getToughness()).isEqualTo(4);
        assertThat(wall.getCard().getColor()).isNull();
        assertThat(wall.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Excess opposing creatures reduce the cost to one white mana")
    void excessReductionLeavesWhiteManaCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new WallOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanents(player1, "Wall")).hasSize(1);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Wall Off");
    }

    @Test
    @DisplayName("Excess cost reduction cannot pay the required white mana")
    void excessReductionDoesNotRemoveWhiteRequirement() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new WallOff()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.assertInHand(player1, "Wall Off");
        assertThat(findPermanents(player1, "Wall")).isEmpty();
        harness.assertLife(player1, 20);
    }
}
