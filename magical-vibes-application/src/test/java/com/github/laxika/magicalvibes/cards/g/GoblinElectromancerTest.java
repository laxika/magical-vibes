package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinElectromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Instant spells cost {1} less to cast")
    void instantCostsOneLess() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Angel's Mercy {2}{W}{W} reduced to {1}{W}{W}
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel's Mercy");
    }

    @Test
    @DisplayName("Sorcery spells cost {1} less to cast")
    void sorceryCostsOneLess() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Divination {2}{U} reduced to {1}{U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Grizzly Bears {1}{G} unaffected
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce opponent's instant and sorcery costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Angel's Mercy still costs {2}{W}{W} for the opponent
        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Electromancers reduce a sorcery's cost by {2}")
    void reductionsStack() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Divination {2}{U} reduced to {U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Instant is not castable when mana falls short of the reduced cost")
    void notCastableBelowReducedCost() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        // Reduced to {1}{W}{W}; two white is not enough
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
