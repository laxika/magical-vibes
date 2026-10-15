package com.github.laxika.magicalvibes.cards.g;

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

        assertThat(ogre.getPowerModifier()).isEqualTo(1);
        assertThat(ogre.getEffectivePower()).isEqualTo(5);
        assertThat(ogre.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost Goblin Wardriver itself")
    void battleCryDoesNotBoostSelf() {
        Permanent wardriver = addCreatureReady(player1, new GoblinWardriver());

        addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(wardriver.getPowerModifier()).isEqualTo(0);
        assertThat(wardriver.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void battleCryDoesNotBoostNonattackersOrOpponents() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent nonattacker = addCreatureReady(player1, new GoblinWardriver());
        Permanent opponent = addCreatureReady(player2, new GoblinWardriver());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    void nonattackingWardriverDoesNotTrigger() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent attacker = addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
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

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(third.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(third.getToughnessModifier()).isZero();
    }

    @Test
    void battleCryResolvesAfterWardriverIsDestroyed() {
        Permanent wardriver = addCreatureReady(player1, new GoblinWardriver());
        Permanent ogre = addCreatureReady(player1, new OgreResister());
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(ogre.getPowerModifier()).isZero();
            harness.passPriority(player1);
            harness.castInstant(player2, 0, wardriver.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Goblin Wardriver");
            resolveAllTriggers();
        });

        assertThat(ogre.getPowerModifier()).isEqualTo(1);
        assertThat(ogre.getToughnessModifier()).isZero();
    }

    @Test
    void battleCryLastsThroughEndStepAndExpiresAtCleanup() {
        addCreatureReady(player1, new GoblinWardriver());
        Permanent ogre = addCreatureReady(player1, new OgreResister());

        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.END_STEP);

        assertThat(ogre.getPowerModifier()).isEqualTo(1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(ogre.getPowerModifier()).isZero();
        assertThat(ogre.getToughnessModifier()).isZero();
    }
}
