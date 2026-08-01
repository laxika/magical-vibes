package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelmOfAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells cost {1} less for the controller")
    void creatureCostsOneLessForController() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Grizzly Bears {1}{G} reduced to {G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Colored mana is not reduced — Lightning Bolt still needs {R}")
    void coloredManaNotReduced() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sorcery spells cost {1} less for the controller")
    void sorceryCostsOneLessForController() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Divination {2}{U} reduced to {1}{U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Reduction is symmetric — opponents' spells are cheaper too")
    void opponentSpellsAreAlsoReduced() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Two Helms reduce a creature's cost by {2}")
    void reductionsStack() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Divination {2}{U} reduced to {U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Spell is not castable when mana falls short of the reduced cost")
    void notCastableBelowReducedCost() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Divination reduced to {1}{U}; only {U} is not enough
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
