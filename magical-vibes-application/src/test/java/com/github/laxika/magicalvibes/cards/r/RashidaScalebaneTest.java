package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RashidaScalebane.class, ShivanDragon.class, GrizzlyBears.class})
class RashidaScalebaneTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking Dragon and gains life equal to its power")
    void destroysAttackingDragon() {
        Permanent rashida = addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        assertThat(rashida.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        harness.assertInGraveyard(player2, "Shivan Dragon");
        harness.assertLife(player1, lifeBefore + 5);
    }

    @Test
    @DisplayName("Destroys a blocking Dragon")
    void destroysBlockingDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setBlocking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        harness.assertInGraveyard(player2, "Shivan Dragon");
        harness.assertLife(player1, lifeBefore + 5);
    }

    @Test
    @DisplayName("Cannot target a Dragon that is neither attacking nor blocking")
    void cannotTargetIdleDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dragon.getId()))
                .hasMessageContaining("Target must be an attacking or blocking Dragon");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Dragon creature")
    void cannotTargetAttackingNonDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .hasMessageContaining("Target must be an attacking or blocking Dragon");
    }

    @Test
    @DisplayName("Uses the Dragon's effective power for life gain")
    void gainsLifeEqualToEffectivePower() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setAttacking(true);
        dragon.setPowerModifier(3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        harness.assertLife(player1, lifeBefore + 8);
    }

    @Test
    @DisplayName("Destroys an attacking Dragon despite a regeneration shield")
    void destroysDragonDespiteRegenerationShield() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setAttacking(true);
        dragon.setRegenerationShield(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        harness.assertInGraveyard(player2, "Shivan Dragon");
    }

    @Test
    @DisplayName("Still gains life when an indestructible Dragon cannot be destroyed")
    void gainsLifeWhenDragonIsIndestructible() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setBlocking(true);
        dragon.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Dragon");
        harness.assertNotInGraveyard(player2, "Shivan Dragon");
        harness.assertLife(player1, lifeBefore + 5);
    }

    @Test
    @DisplayName("Fizzles when the Dragon is no longer attacking or blocking at resolution")
    void fizzlesWhenDragonLeavesCombat() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        dragon.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        dragon.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Dragon");
        harness.assertNotInGraveyard(player2, "Shivan Dragon");
        harness.assertLife(player1, lifeBefore);
    }
}
