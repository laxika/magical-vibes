package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DogmeatEverLoyal;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KelloggDangerousMind.class, DogmeatEverLoyal.class, Treasure.class})
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
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
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
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
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

    @Test
    @DisplayName("Leaving before resolution prevents the control effect but does not refund Treasures")
    void leavingBeforeResolutionDoesNotGainControl() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
        addTreasures(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId());
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kellogg));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Four Treasures cannot pay the activation cost")
    void insufficientTreasuresCannotActivate() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
        addTreasures(4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The ability cannot be activated during combat")
    void cannotActivateDuringCombat() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
        addTreasures(5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, newly controlled Kellogg can activate without tapping or paying mana")
    void tappedSummoningSickKelloggCanActivate() {
        Permanent kellogg = harness.addToBattlefieldAndReturn(player1, new KelloggDangerousMind());
        kellogg.tap();
        kellogg.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
        addTreasures(5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(kellogg.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A pending activation prevents another sorcery-speed activation")
    void cannotActivateWithNonemptyStack() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        Permanent target = addCreatureReady(player2, new DogmeatEverLoyal());
        addTreasures(5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId());
        addTreasures(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(kellogg), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("An attack trigger still creates its Treasure after Kellogg leaves")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent kellogg = addCreatureReady(player1, new KelloggDangerousMind());
        declareAttackers(List.of(0));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kellogg));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kellogg);
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
