package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderdarkBasilisk.class, HillGiant.class})
class UnderdarkBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        hillGiant.setAttacking(true);

        Permanent basilisk = addCreatureReady(player2, new UnderdarkBasilisk());
        basilisk.setBlocking(true);
        basilisk.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hillGiant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(basilisk);
    }
}
