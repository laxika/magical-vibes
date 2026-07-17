package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaribouRangeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability creates a 0/1 white Caribou token")
    void enchantedLandCreatesCaribouToken() {
        Permanent forest = attachedRange(player1).forest();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Activate the granted ability on the forest (permanent index 0, ability index 0).
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Caribou"))
                .singleElement()
                .satisfies(caribou -> {
                    assertThat(caribou.getCard().getPower()).isEqualTo(0);
                    assertThat(caribou.getCard().getToughness()).isEqualTo(1);
                    assertThat(caribou.getCard().isToken()).isTrue();
                });
    }

    @Test
    @DisplayName("Sacrificing a Caribou token to the aura gains 1 life")
    void sacrificeCaribouGainsLife() {
        Range range = attachedRange(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Make a Caribou token via the granted ability.
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(range.aura());

        // Sacrifice the Caribou token (aura's own ability, no cost, no target).
        harness.activateAbility(player1, auraIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Caribou"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Caribou"));
    }

    @Test
    @DisplayName("Cannot enchant a land you do not control")
    void cannotEnchantOpponentsLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, java.util.List.of(new CaribouRange()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private record Range(Permanent forest, Permanent aura) {
    }

    private Range attachedRange(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Forest());
        Permanent forest = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new CaribouRange());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return new Range(forest, aura);
    }
}
