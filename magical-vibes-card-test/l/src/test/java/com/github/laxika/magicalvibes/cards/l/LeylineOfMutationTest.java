package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfMutation.class, CrawWurm.class})
class LeylineOfMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfMutation()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of Mutation");
    }

    @Test
    @DisplayName("Leyline lets its controller cast a spell for the five-color alternative cost")
    void castsSpellForFiveColorAlternativeCost() {
        harness.addToBattlefield(player1, new LeylineOfMutation());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Craw Wurm");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void openingHandPlacementCanBeDeclined() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfMutation()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of Mutation");
        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of Mutation");
    }

    @Test
    void alternativeCostAppliesToEnchantmentSpells() {
        harness.addToBattlefield(player1, new LeylineOfMutation());
        harness.setHand(player1, List.of(new LeylineOfMutation()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof LeylineOfMutation).hasSize(2);
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    void normalManaCostRemainsAvailable() {
        harness.addToBattlefield(player1, new LeylineOfMutation());
        harness.castFromHand(player1, new LeylineOfMutation(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof LeylineOfMutation).hasSize(2);
    }

    @Test
    void alternativeCostCanBeChosenWhenNormalCostIsAlsoAffordable() {
        harness.addToBattlefield(player1, new LeylineOfMutation());
        harness.setHand(player1, List.of(new LeylineOfMutation()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof LeylineOfMutation).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }
}
