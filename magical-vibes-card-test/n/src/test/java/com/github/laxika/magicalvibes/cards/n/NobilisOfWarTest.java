package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NobilisOfWar.class, GrizzlyBears.class})
class NobilisOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +2/+0, including Nobilis itself")
    void boostsAttackingCreatures() {
        Permanent nobilis = addCreatureReady(player1, new NobilisOfWar()); // 3/4
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        // Nobilis (3/4) attacking gets +2/+0 from its own static effect = 5/4
        assertThat(gqs.getEffectivePower(gd, nobilis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nobilis)).isEqualTo(4);

        // Grizzly Bears (2/2) attacking gets +2/+0 = 4/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new NobilisOfWar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        // Only Nobilis attacks (index 0), not bears
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new NobilisOfWar());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-attacking Nobilis boosts another attacking Nobilis")
    void nonAttackingSourceBoostsAttacker() {
        Permanent source = addCreatureReady(player1, new NobilisOfWar());
        Permanent attacker = addCreatureReady(player1, new NobilisOfWar());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple copies boost each attacking creature once per copy")
    void multipleCopiesStack() {
        Permanent first = addCreatureReady(player1, new NobilisOfWar());
        Permanent second = addCreatureReady(player1, new NobilisOfWar());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost applies to combat damage and ends when combat ends")
    void boostEndsAfterCombat() {
        Permanent nobilis = addCreatureReady(player1, new NobilisOfWar());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 15);
        assertThat(gqs.getEffectivePower(gd, nobilis)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nobilis)).isEqualTo(4);
    }

}
