package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaribouRange.class, Forest.class})
class CaribouRangeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Caribou Range attaches it to a land you control")
    void resolvesOnControlledLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        CaribouRange range = new CaribouRange();
        harness.setHand(player1, List.of(range));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == range)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Enchanted land's granted ability creates a 0/1 white Caribou token")
    void enchantedLandCreatesCaribouToken() {
        Permanent forest = attachedRange(player1).forest();
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Activate the granted ability on the forest (permanent index 0, ability index 0).
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(otherForest.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Caribou"))
                .singleElement()
                .satisfies(caribou -> {
                    assertThat(caribou.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(caribou.getCard().getPower()).isEqualTo(0);
                    assertThat(caribou.getCard().getToughness()).isEqualTo(1);
                    assertThat(caribou.getCard().getSubtypes()).containsExactly(CardSubtype.CARIBOU);
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
        harness.assertNotInGraveyard(player1, "Caribou");
        harness.assertNotOnBattlefield(player1, "Caribou");
    }

    @Test
    @DisplayName("The life-gain ability cannot be activated without a Caribou token")
    void cannotSacrificeWithoutCaribouToken() {
        Range range = attachedRange(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(range.aura());

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a land you do not control")
    void cannotEnchantOpponentsLand() {
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CaribouRange()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private record Range(Permanent forest, Permanent aura) {
    }

    private Range attachedRange(Player player) {
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        Permanent aura = new Permanent(new CaribouRange());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return new Range(forest, aura);
    }
}
