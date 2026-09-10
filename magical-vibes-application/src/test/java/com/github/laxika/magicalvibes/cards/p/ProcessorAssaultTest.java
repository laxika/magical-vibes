package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProcessorAssault.class, GrizzlyBears.class, PathToExile.class})
class ProcessorAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an opponent-owned exiled card into its owner's graveyard and deals 5 damage")
    void paysExileCostAndDealsFiveDamage() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();

        harness.setHand(player1, List.of(new ProcessorAssault()));
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithChosenAdditionalCostObject(player1, 0, target.getId(), exiledCard.getId());

        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot use a card owned by the caster as the additional cost")
    void cannotUseOwnExiledCard() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();

        harness.setHand(player1, List.of(new ProcessorAssault()));
        harness.setExile(player1, List.of(exiledCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithChosenAdditionalCostObject(
                player1, 0, target.getId(), exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent owns");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
