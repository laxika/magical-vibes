package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinWardriver.class, OgreResister.class, GoForTheThroat.class})
class GoblinWardriverTest extends BaseCardTest {

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        addCreatureReady(player1, new GoblinWardriver());

        Permanent ogre = addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost Goblin Wardriver itself")
    void battleCryDoesNotBoostSelf() {
        Permanent wardriver = addCreatureReady(player1, new GoblinWardriver());

        addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wardriver)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wardriver)).isEqualTo(0);
    }

    @Test
    void battleCryDoesNotBoostNonattackersOrOpponents() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent nonattacker = addCreatureReady(player1, new GoblinWardriver());
        Permanent opponent = addCreatureReady(player2, new GoblinWardriver());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
    }

    @Test
    void nonattackingWardriverDoesNotTrigger() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent attacker = addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
    }

    @Test
    void multipleWardriversBoostEachOtherAndStackOnOtherAttackers() {
        Permanent first = addCreatureReady(player1, new GoblinWardriver());
        Permanent second = addCreatureReady(player1, new GoblinWardriver());
        Permanent third = addCreatureReady(player1, new GoblinWardriver());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            assertThat(gd.stack).hasSize(3);
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, third)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, third)).isZero();
    }

    @Test
    void battleCryResolvesAfterWardriverIsDestroyed() {
        Permanent wardriver = addCreatureReady(player1, new GoblinWardriver());
        Permanent ogre = addCreatureReady(player1, new OgreResister());
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isZero();
            harness.passPriority(player1);
            harness.castInstant(player2, 0, wardriver.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Goblin Wardriver");
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isZero();
    }

    @Test
    void battleCryLastsThroughEndStepAndExpiresAtCleanup() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent ogre = addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.END_STEP);

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isEqualTo(1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isZero();
    }
}
