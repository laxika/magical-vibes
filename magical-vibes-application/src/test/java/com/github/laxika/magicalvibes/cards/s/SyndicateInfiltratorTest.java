package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicateInfiltrator.class, Forest.class, GrizzlyBears.class, HillGiant.class, Murder.class, Shock.class})
class SyndicateInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with five distinct graveyard mana values")
    void gainsBoostAtThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new HillGiant()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get the graveyard bonus with fewer than five distinct mana values")
    void doesNotGainBonusBelowThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new HillGiant(), new Murder(), new GrizzlyBears()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }
}
