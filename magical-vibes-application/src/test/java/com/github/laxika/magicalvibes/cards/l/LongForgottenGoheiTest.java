package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.h.HonorWornShaku;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongForgottenGohei.class, GlacialRay.class, HonorWornShaku.class,
        LanternKami.class, HumbleBudoka.class})
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
        // Honor-Worn Shaku {3} stays {3}; only {2} available.

        assertThatThrownBy(() -> harness.castFromHand(player1, new HonorWornShaku(), "{2}"))
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
        Permanent kami = addCreatureReady(player1, new LanternKami());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        // Lantern Kami is a 1/1 Spirit -> 2/2.
        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Spirit creatures are not boosted")
    void doesNotBoostNonSpirits() {
        Permanent budoka = addCreatureReady(player1, new HumbleBudoka());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        assertThat(gqs.getEffectivePower(gd, budoka)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, budoka)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Spirits are not boosted")
    void doesNotBoostOpponentSpirits() {
        Permanent kami = addCreatureReady(player2, new LanternKami());
        harness.addToBattlefield(player1, new LongForgottenGohei());

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(1);
    }
}
