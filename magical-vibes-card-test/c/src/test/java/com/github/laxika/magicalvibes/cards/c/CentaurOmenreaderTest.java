package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GravePeril;
import com.github.laxika.magicalvibes.cards.l.LlanowarEmpath;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurOmenreader.class, LlanowarEmpath.class, GravePeril.class})
class CentaurOmenreaderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Centaur Omenreader reduces creature spell costs by {2}")
    void tappedSourceReducesCreatureSpellCost() {
        Permanent omenreader = harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader());
        omenreader.tap();
        harness.castFromHand(player1, new LlanowarEmpath(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapped Centaur Omenreader does not reduce creature spell costs")
    void untappedSourceDoesNotReduceCreatureSpellCost() {
        harness.addToBattlefield(player1, new CentaurOmenreader());

        assertThatThrownBy(() -> harness.castFromHand(player1, new LlanowarEmpath(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Centaur Omenreader does not reduce noncreature spell costs")
    void tappedSourceDoesNotReduceNoncreatureSpellCost() {
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();

        assertThatThrownBy(() -> harness.castFromHand(player1, new GravePeril(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Centaur Omenreader does not reduce an opponent's creature spell")
    void tappedSourceDoesNotReduceOpponentsCreatureSpell() {
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromHand(player2, new LlanowarEmpath(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple tapped Omenreaders stack their reductions without reducing colored mana")
    void multipleTappedSourcesStackReductions() {
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();

        harness.castFromHand(player1, new LlanowarEmpath(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess generic reduction cannot pay the required green mana")
    void excessReductionDoesNotRemoveColoredRequirement() {
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();

        assertThatThrownBy(() -> harness.castFromHand(player1, new LlanowarEmpath(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapping Omenreader ends its cost reduction immediately")
    void untappingSourceEndsReduction() {
        Permanent omenreader = harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader());
        omenreader.tap();
        omenreader.untap();

        assertThatThrownBy(() -> harness.castFromHand(player1, new LlanowarEmpath(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An untapped Omenreader does not contribute to another Omenreader's reduction")
    void onlyTappedSourcesContribute() {
        harness.addToBattlefieldAndReturn(player1, new CentaurOmenreader()).tap();
        harness.addToBattlefield(player1, new CentaurOmenreader());

        assertThatThrownBy(() -> harness.castFromHand(player1, new LlanowarEmpath(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
