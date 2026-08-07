package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LongForgottenGoheiTest extends BaseCardTest {

    @Test
    @DisplayName("Arcane spells you cast cost {1} less")
    void arcaneSpellCostsOneLess() {
        harness.addToBattlefield(player1, new LongForgottenGohei());
        // Glacial Ray {1}{R} reduced to {R}
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Arcane spells are not reduced")
    void nonArcaneSpellNotReduced() {
        harness.addToBattlefield(player1, new LongForgottenGohei());
        // Divination {2}{U} stays {2}{U}; only {1}{U} available
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' Arcane spells are not reduced")
    void opponentArcaneSpellNotReduced() {
        harness.addToBattlefield(player1, new LongForgottenGohei());
        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spirit creatures you control get +1/+1")
    void boostsOwnSpirits() {
        Permanent geist = addCreatureReady(player1, new ApothecaryGeist());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        // Apothecary Geist is a 2/3 Spirit -> 3/4
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-Spirit creatures are not boosted")
    void doesNotBoostNonSpirits() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Spirits are not boosted")
    void doesNotBoostOpponentSpirits() {
        Permanent geist = addCreatureReady(player2, new ApothecaryGeist());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }
}
