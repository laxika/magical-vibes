package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurasaBehemoth.class, Forest.class, GrizzlyBears.class})
class MurasaBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus without a land card in its controller's graveyard")
    void noBonusWithoutLandCard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertStats(behemoth, 5, 5);
    }

    @Test
    @DisplayName("Gets +3/+3 with a land card in its controller's graveyard")
    void getsBonusWithLandCard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player1, List.of(new Forest()));

        assertStats(behemoth, 8, 8);
    }

    @Test
    @DisplayName("Only checks its controller's graveyard")
    void ignoresOpponentsGraveyard() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player2, List.of(new Forest()));

        assertStats(behemoth, 5, 5);
    }

    @Test
    @DisplayName("The bonus turns on and off as land cards enter and leave the graveyard")
    void bonusTracksGraveyardChanges() {
        Permanent behemoth = addBehemoth();
        assertStats(behemoth, 5, 5);

        harness.setGraveyard(player1, List.of(new Forest()));
        assertStats(behemoth, 8, 8);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        assertStats(behemoth, 5, 5);

        harness.setGraveyard(player1, List.of(new Forest()));
        assertStats(behemoth, 8, 8);
    }

    @Test
    @DisplayName("Multiple land cards still grant only one +3/+3 bonus")
    void multipleLandsDoNotMultiplyBonus() {
        Permanent behemoth = addBehemoth();
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        assertStats(behemoth, 8, 8);
    }

    @Test
    @DisplayName("The bonus applies only to each Behemoth whose controller has a land card")
    void bonusDoesNotAffectOtherCreatures() {
        Permanent behemoth = addBehemoth();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBehemoth = harness.addToBattlefieldAndReturn(player2, new MurasaBehemoth());
        harness.setGraveyard(player1, List.of(new Forest()));

        assertStats(behemoth, 8, 8);
        assertStats(bears, 2, 2);
        assertStats(opposingBehemoth, 5, 5);

        harness.setGraveyard(player2, List.of(new Forest()));
        assertStats(opposingBehemoth, 8, 8);
        assertStats(behemoth, 8, 8);
    }

    @Test
    @DisplayName("The boosted Behemoth tramples over a blocker")
    void boostedBehemothTramples() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new Forest()));
        addCreatureReady(player1, new MurasaBehemoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Murasa Behemoth");
    }

    private Permanent addBehemoth() {
        return harness.addToBattlefieldAndReturn(player1, new MurasaBehemoth());
    }

    private void assertStats(Permanent behemoth, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(toughness);
    }
}
