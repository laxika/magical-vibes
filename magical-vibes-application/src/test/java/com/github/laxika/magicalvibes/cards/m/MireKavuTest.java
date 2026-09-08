package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MireKavuTest extends BaseCardTest {

    @Test
    void noBoostWithoutSwamp() {
        harness.addToBattlefield(player1, new MireKavu());
        harness.addToBattlefield(player1, new Forest());

        Permanent mireKavu = findPermanent(player1, "Mire Kavu");
        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }

    @Test
    void getsBoostWithSwamp() {
        harness.addToBattlefield(player1, new MireKavu());
        harness.addToBattlefield(player1, new Swamp());

        Permanent mireKavu = findPermanent(player1, "Mire Kavu");
        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);
    }

    @Test
    void opponentSwampDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new MireKavu());
        harness.addToBattlefield(player2, new Swamp());

        Permanent mireKavu = findPermanent(player1, "Mire Kavu");
        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }

    @Test
    void losesBoostWhenSwampLeaves() {
        harness.addToBattlefield(player1, new MireKavu());
        harness.addToBattlefield(player1, new Swamp());

        Permanent mireKavu = findPermanent(player1, "Mire Kavu");
        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Swamp"));

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }
}
