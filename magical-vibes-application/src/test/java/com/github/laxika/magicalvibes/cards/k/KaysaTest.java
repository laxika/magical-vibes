package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KaysaTest extends BaseCardTest {

    @Test
    @DisplayName("Kaysa boosts itself, since it is green")
    void boostsSelf() {
        Permanent kaysa = addCreatureReady(player1, new Kaysa());

        assertThat(gqs.getEffectivePower(gd, kaysa)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kaysa)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boosts another green creature you control, and the boost goes away when Kaysa leaves")
    void boostsOtherGreenCreature() {
        Permanent kaysa = addCreatureReady(player1, new Kaysa());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(kaysa);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a non-green creature you control")
    void doesNotBoostNonGreenCreature() {
        addCreatureReady(player1, new Kaysa());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost an opponent's green creature")
    void doesNotBoostOpponentGreenCreature() {
        addCreatureReady(player1, new Kaysa());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }
}
