package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LatchkeyFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrogtosserBanneretTest extends BaseCardTest {

    // ===== Goblin cost reduction =====

    @Test
    @DisplayName("Goblin spells cost {1} less with Frogtosser Banneret on the battlefield")
    void goblinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Goblin Elite Infantry costs {1}{R} — with {1} reduction it costs {R}
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Goblin spell is not castable when mana falls short of the reduced cost")
    void goblinNotCastableWithoutEnoughMana() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Goblin Elite Infantry reduced to {R}; no mana means it cannot be cast
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Rogue cost reduction =====

    @Test
    @DisplayName("Rogue spells cost {1} less with Frogtosser Banneret on the battlefield")
    void rogueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Latchkey Faerie (Faerie Rogue) costs {3}{U} — with {1} reduction it costs {2}{U}
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Latchkey Faerie");
    }

    // ===== Non-matching spells are not reduced =====

    @Test
    @DisplayName("Non-Goblin, non-Rogue spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Grizzly Bears (Bear) costs {1}{G} — not reduced; one green is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Bannerets reduce a Goblin spell's cost by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Latchkey Faerie {3}{U} — with {2} reduction the cost is {1}{U}
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Latchkey Faerie");
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Frogtosser Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Opponent's Goblin Elite Infantry should still cost {1}{R}; one red is not enough
        harness.setHand(player2, List.of(new GoblinEliteInfantry()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
