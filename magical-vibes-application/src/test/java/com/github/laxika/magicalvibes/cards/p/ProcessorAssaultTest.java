package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.r.RuinProcessor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProcessorAssault.class, GrizzlyBears.class, PathToExile.class,
        CoralhelmGuide.class, RuinProcessor.class})
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

    @Test
    @DisplayName("Deals exactly 5 damage to a creature controlled by the caster")
    void dealsExactlyFiveDamageToOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new RuinProcessor());
        CoralhelmGuide paidCard = new CoralhelmGuide();
        CoralhelmGuide remainingCard = new CoralhelmGuide();
        harness.setHand(player1, List.of(new ProcessorAssault()));
        harness.setExile(player2, List.of(paidCard, remainingCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithChosenAdditionalCostObject(player1, 0, target.getId(), paidCard.getId());

        assertThat(gd.findExiledCard(paidCard.getId())).isNull();
        assertThat(gd.findExiledCard(remainingCard.getId())).isNotNull();
        harness.assertInGraveyard(player2, "Coralhelm Guide");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ruin Processor");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Processor Assault");
    }

    @Test
    @DisplayName("Cannot cast without paying the exile cost")
    void cannotCastWithoutExiledCard() {
        var target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new ProcessorAssault()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorceryWithChosenAdditionalCostObject(
                player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent owns");

        harness.assertInHand(player1, "Processor Assault");
        harness.assertOnBattlefield(player2, "Coralhelm Guide");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a player target without consuming the additional cost")
    void cannotTargetPlayer() {
        CoralhelmGuide exiledCard = new CoralhelmGuide();
        harness.setHand(player1, List.of(new ProcessorAssault()));
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorceryWithChosenAdditionalCostObject(
                player1, 0, player2.getId(), exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Processor Assault");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
