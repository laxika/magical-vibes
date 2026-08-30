package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MahaItsFeathersNight.class, GloriousAnthem.class, HillGiant.class})
class MahaItsFeathersNightTest extends BaseCardTest {

    @Test
    void setsOpponentsCreaturesBaseToughnessToOne() {
        Permanent maha = addCreatureReady(player1, new MahaItsFeathersNight());
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.addToBattlefield(player2, new GloriousAnthem());

        assertThat(maha.getEffectivePower()).isEqualTo(6);
        assertThat(maha.getEffectiveToughness()).isEqualTo(5);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
