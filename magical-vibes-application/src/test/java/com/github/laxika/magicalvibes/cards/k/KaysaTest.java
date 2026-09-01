package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kaysa.class, ElvishRanger.class, StormCrow.class})
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
        Permanent ranger = addCreatureReady(player1, new ElvishRanger());

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(kaysa);

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost a non-green creature you control")
    void doesNotBoostNonGreenCreature() {
        addCreatureReady(player1, new Kaysa());
        Permanent crow = addCreatureReady(player1, new StormCrow());

        assertThat(gqs.getEffectivePower(gd, crow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crow)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's green creature")
    void doesNotBoostOpponentGreenCreature() {
        addCreatureReady(player1, new Kaysa());
        Permanent opponentRanger = addCreatureReady(player2, new ElvishRanger());

        assertThat(gqs.getEffectivePower(gd, opponentRanger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentRanger)).isEqualTo(1);
    }

    @Test
    void bonusesStack() {
        Permanent firstKaysa = addCreatureReady(player1, new Kaysa());
        Permanent secondKaysa = addCreatureReady(player1, new Kaysa());
        Permanent ranger = addCreatureReady(player1, new ElvishRanger());

        assertThat(gqs.getEffectivePower(gd, firstKaysa)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstKaysa)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondKaysa)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondKaysa)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
    }
}
