package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerraRavager.class, Forest.class, Mountain.class})
class TerraRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 for the defending player's lands")
    void boostsByDefendingPlayersLands() {
        Permanent ravager = addCreatureReady(player1, new TerraRavager());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ravager)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ravager)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets no boost when the defending player controls no lands")
    void noBoostWithoutDefendingLands() {
        Permanent ravager = addCreatureReady(player1, new TerraRavager());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ravager)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ravager)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts defending lands when the trigger resolves and keeps that boost fixed")
    void countsLandsAtResolution() {
        Permanent ravager = addCreatureReady(player1, new TerraRavager());
        harness.addToBattlefield(player2, new Mountain());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ravager)));
            assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(0);

            harness.addToBattlefield(player2, new Mountain());
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, ravager)).isEqualTo(4);

            harness.addToBattlefield(player2, new Mountain());
            assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Counts player one's lands when player two attacks")
    void countsDefendingLandsWhenPlayerTwoAttacks() {
        Permanent ravager = addCreatureReady(player2, new TerraRavager());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(ravager)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ravager)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ravager = addCreatureReady(player1, new TerraRavager());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ravager)));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ravager)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ravager)).isEqualTo(4);
    }
}
