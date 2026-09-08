package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranGuard.class, BalduvianBears.class, SnowCoveredPlains.class, SnowCoveredMountain.class})
class KjeldoranGuardTest extends BaseCardTest {

    private Permanent addGuardReady() {
        return addCreatureReady(player1, new KjeldoranGuard());
    }

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(TurnStep.BEGINNING_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), EnumSet.of(TurnStep.BEGINNING_OF_COMBAT));
    }

    private Permanent snowLandOnDefender() {
        return harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
    }

    private Permanent nonSnowLandOnDefender() {
        Permanent land = new Permanent(new SnowCoveredPlains());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        gd.playerBattlefields.get(player2.getId()).add(land);
        return land;
    }

    private void enterOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateAfterCombat() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when defending player controls a snow land")
    void cannotActivateWhenDefenderHasSnowLand() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        snowLandOnDefender();

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow lands");
    }

    @Test
    @DisplayName("Non-snow land does not block activation")
    void nonsnowLandDoesNotBlockActivation() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        nonSnowLandOnDefender();

        int basePower = gqs.getEffectivePower(gd, target);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, target.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Gives target creature +1/+1 until end of turn during combat")
    void pumpsTargetDuringCombat() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, target.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);
        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player2, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, target.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, after));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Guard");
    }

    @Test
    @DisplayName("Sacrifices itself when the pumped creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, after));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Guard");
        harness.assertInGraveyard(player1, "Kjeldoran Guard");
        harness.assertInHand(player1, after.getCard().getName());
    }

    @Test
    @DisplayName("Does not sacrifice when the pumped creature stays on the battlefield")
    void doesNotSacrificeIfTargetStays() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Guard");
        harness.assertOnBattlefield(player1, target.getCard().getName());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent guard = addGuardReady();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checksDefendingPlayerWhenControllerIsNotActivePlayer() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        enterOpponentCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void delayedSacrificeExpiresAtEndOfTurn() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        Permanent after = gqs.findPermanentById(gd, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, after));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Guard");
    }

    @Test
    void allowsActivationWhenOnlyAttackingPlayerControlsSnowLand() {
        Permanent guard = addGuardReady();
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());

        int basePower = gqs.getEffectivePower(gd, target);

        enterOpponentCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, target.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, target.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
