package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

    @Test
    @DisplayName("Kindred and artifact count separately, and duplicate types count only once")
    void countsDistinctTypesIncludingKindred() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AltarOfTheGoyf(), new Millstone()));
        harness.setGraveyard(player2, List.of(new AltarOfTheGoyf()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("X is determined on resolution and the resulting boost lasts only this turn")
    void evaluatesTypesOnResolutionAndKeepsBoostFixedUntilCleanup() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));

        declareAttackers(List.of(1));
        harness.setGraveyard(player2, List.of(new AltarOfTheGoyf(), new Shock()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Altar independently boosts the lone attacker")
    void multipleAltarsGiveCumulativeBoosts() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AltarOfTheGoyf()));

        declareAttackers(List.of(2));
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's lone attacker does not receive the boost")
    void doesNotBoostOpposingLoneAttacker() {
        harness.addToBattlefield(player1, new AltarOfTheGoyf());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AltarOfTheGoyf()));

        declareAttackers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample is lost when the Altar leaves the battlefield")
    void stopsGrantingTrampleWhenAltarLeaves() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheGoyf());
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        assertThat(gqs.hasKeyword(gd, goyf, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(altar);

        assertThat(gqs.hasKeyword(gd, goyf, Keyword.TRAMPLE)).isFalse();
    }
}
