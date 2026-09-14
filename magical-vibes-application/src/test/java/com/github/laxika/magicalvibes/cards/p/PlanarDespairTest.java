package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarDespair.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        HillGiant.class, MahamotiDjinn.class})
class PlanarDespairTest extends BaseCardTest {

    private void castPlanarDespair() {
        harness.castFromHand(player1, new PlanarDespair(), "{3}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gives all creatures -1/-1 for each distinct basic land type among the caster's lands")
    void debuffsAllCreaturesByDomain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent enemyGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPlanarDespair();

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enemyGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyGiant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts each basic land type once and ignores the opponent's lands")
    void countsDistinctControllerLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPlanarDespair();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts all five basic land types")
    void countsAllFiveBasicLandTypes() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new MahamotiDjinn());

        castPlanarDespair();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has no effect when the caster controls no basic land types")
    void zeroDomainDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPlanarDespair();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Forest());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPlanarDespair();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }
}
