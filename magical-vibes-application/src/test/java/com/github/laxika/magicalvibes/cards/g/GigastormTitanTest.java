package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GigastormTitan.class, Fog.class})
class GigastormTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot pay the full cost with only three generic mana")
    void cannotCastWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new GigastormTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs three less after another spell is cast this turn")
    void costsThreeLessAfterAnotherSpell() {
        harness.setHand(player1, List.of(new Fog(), new GigastormTitan()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Gigastorm Titan"));
    }

    @Test
    @DisplayName("The first spell costs the full four generic and one blue mana")
    void firstSpellPaysFullCost() {
        harness.setHand(player1, List.of(new GigastormTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gigastorm Titan");
    }

    @Test
    @DisplayName("A previously cast creature enables the reduction")
    void previousCreatureEnablesReduction() {
        harness.setHand(player1, List.of(new GigastormTitan(), new GigastormTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The reduction does not remove the blue mana requirement")
    void reductionStillRequiresBlueMana() {
        harness.setHand(player1, List.of(new Fog(), new GigastormTitan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's spell does not enable the reduction")
    void opponentSpellDoesNotEnableReduction() {
        harness.setHand(player1, List.of(new GigastormTitan()));
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell cast on a previous turn does not enable the reduction")
    void reductionExpiresAfterTurn() {
        harness.setHand(player1, List.of(new GigastormTitan(), new GigastormTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
