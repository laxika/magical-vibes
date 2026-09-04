package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FallenAskari;
import com.github.laxika.magicalvibes.cards.g.GoblinSwineRider;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.p.Prosperity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfAwakening.class, FallenAskari.class, GoblinSwineRider.class, Impulse.class,
        PhyrexianWalker.class, Prosperity.class})
class HelmOfAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells cost {1} less for the controller")
    void creatureCostsOneLessForController() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Fallen Askari {1}{B} reduced to {B}.
        harness.castFromHand(player1, new FallenAskari(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Colored mana is not reduced — Goblin Swine-Rider still needs {R}")
    void coloredManaNotReduced() {
        harness.addToBattlefield(player1, new HelmOfAwakening());

        assertThatThrownBy(() -> harness.castFromHand(player1, new GoblinSwineRider(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sorcery spells cost {1} less for the controller")
    void sorceryCostsOneLessForController() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Prosperity with X=1 costs {1}{U}, reduced to {U}.
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Instant spells cost {1} less for the controller")
    void instantCostsOneLessForController() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Impulse {1}{U} reduced to {U}.
        harness.castFromHand(player1, new Impulse(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reduction is symmetric — opponents' spells are cheaper too")
    void opponentSpellsAreAlsoReduced() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.forceActivePlayer(player2);

        // Fallen Askari {1}{B} reduced to {B} for the opponent too.
        harness.castFromHand(player2, new FallenAskari(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two Helms reduce a sorcery's cost by {2}")
    void reductionsStack() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Prosperity with X=2 costs {2}{U}, reduced to {U}.
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spell is not castable when mana falls short of the reduced cost")
    void notCastableBelowReducedCost() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        // Prosperity with X=2 is reduced from {2}{U} to {1}{U}; only {U} is not enough.
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A zero-mana spell remains castable")
    void zeroManaSpellRemainsCastable() {
        harness.addToBattlefield(player1, new HelmOfAwakening());
        harness.setHand(player1, List.of(new PhyrexianWalker()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
