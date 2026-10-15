package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazoretGodseeker.class, AxegrinderGiant.class, GrizzlyBears.class})
class HazoretGodseekerTest extends BaseCardTest {

    @Test
    void startsEnginesWhenItEntersTheBattlefield() {
        addCreatureReady(player1, new HazoretGodseeker());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotAttackBeforeMaxSpeed() {
        addCreatureReady(player1, new HazoretGodseeker());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackAtMaxSpeed() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HazoretGodseeker());
        gd.playerSpeeds.put(player1.getId(), 4);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    void cannotBlockBeforeMaxSpeed() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new HazoretGodseeker());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockAtMaxSpeed() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new HazoretGodseeker());
        gd.playerSpeeds.put(player1.getId(), 4);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void makesLowPowerTargetUnblockable() {
        Permanent hazoret = addCreatureReady(player1, new HazoretGodseeker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hazoret.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void rejectsTargetWithPowerGreaterThanTwo() {
        addCreatureReady(player1, new HazoretGodseeker());
        Permanent target = addCreatureReady(player2, new AxegrinderGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileSummoningSickAndBelowMaxSpeed() {
        Permanent hazoret = harness.addToBattlefieldAndReturn(player1, new HazoretGodseeker());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hazoret.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void abilityDoesNotResolveIfTargetPowerIncreasesAboveTwo() {
        addCreatureReady(player1, new HazoretGodseeker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void increasingPowerAfterResolutionDoesNotRemoveUnblockability() {
        addCreatureReady(player1, new HazoretGodseeker());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void survivesLethalCombatDamageAtMaxSpeed() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new HazoretGodseeker());
        gd.playerSpeeds.put(player1.getId(), 4);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Hazoret, Godseeker");
        harness.assertInGraveyard(player2, "Axegrinder Giant");
        harness.assertLife(player1, 20);
    }

    @Test
    void enteringWithExistingSpeedDoesNotResetIt() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new HazoretGodseeker());

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }
}
