package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclaveGeopede.class, Forest.class})
class SkyclaveGeopedeTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Skyclave Geopede +2/+2 until end of turn")
    void landfallBoostsSkyclaveGeopede() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(5);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Skyclave Geopede")
    void opponentLandDoesNotTrigger() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(geopede.getEffectivePower()).isEqualTo(5);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each land entering without being played adds another landfall boost")
    void multipleLandEntriesStackBoosts() {
        Permanent geopede = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(geopede.getEffectivePower()).isEqualTo(3);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(1);

        resolveAllTriggers();

        assertThat(geopede.getEffectivePower()).isEqualTo(7);
        assertThat(geopede.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Each Skyclave Geopede receives its own landfall boost")
    void multipleGeopedesEachGetOneBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkyclaveGeopede());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(5);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A landfall-boosted Skyclave Geopede tramples over a blocker")
    void boostedGeopedeTramplesOverBlocker() {
        addCreatureReady(player1, new SkyclaveGeopede());
        Permanent blocker = addCreatureReady(player2, new SkyclaveGeopede());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Skyclave Geopede");
        harness.assertInGraveyard(player2, "Skyclave Geopede");
    }
}
