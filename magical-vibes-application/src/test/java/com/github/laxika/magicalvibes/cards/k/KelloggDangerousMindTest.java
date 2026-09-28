package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KelloggDangerousMind.class, GrizzlyBears.class, Treasure.class})
class KelloggDangerousMindTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure token")
    void attackingCreatesTreasure() {
        addCreatureReady(player1, new KelloggDangerousMind());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing five Treasures gains control of a target creature")
    void sacrificesFiveTreasuresToGainControl() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addTreasures(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The stolen creature returns when Kellogg leaves the battlefield")
    void controlEndsWhenKelloggLeavesBattlefield() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addTreasures(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kellogg));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    private void addTreasures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Treasure());
        }
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
