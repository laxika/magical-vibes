package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfTheGoyf.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class,
        Tarmogoyf.class})
class AltarOfTheGoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Lhurgoyf creatures you control have trample")
    void grantsTrampleToOwnLhurgoyfs() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent ownGoyf = addCreatureReady(player1, new Tarmogoyf());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingGoyf = addCreatureReady(player2, new Tarmogoyf());

        assertThat(gqs.hasKeyword(gd, ownGoyf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGoyf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An attacking creature gets +X/+X for card types in all graveyards")
    void boostsLoneAttackerByGraveyardCardTypes() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Millstone(), new Tarmogoyf()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost does not trigger when more than one creature attacks")
    void doesNotBoostWhenAttackIsNotAlone() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Millstone(), new Tarmogoyf()));

        declareAttackers(List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, firstBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondBears)).isEqualTo(2);
    }
}
