package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.s.SpiritEnDal;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AugurIlVec.class, BlindPhantasm.class, SpiritEnDal.class})
class AugurIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it during upkeep gains 4 life")
    void sacrificeDuringUpkeepGainsFourLife() {
        addCreatureReady(player1, new AugurIlVec());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Augur il-Vec");
        harness.assertInGraveyard(player1, "Augur il-Vec");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Augur is sacrificed immediately but gains life only on resolution")
    void sacrificeCostIsPaidBeforeLifeGainResolves() {
        Permanent augur = harness.addToBattlefieldAndReturn(player2, new AugurIlVec());
        augur.setSummoningSick(true);
        augur.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);

        harness.assertNotOnBattlefield(player2, "Augur il-Vec");
        harness.assertInGraveyard(player2, "Augur il-Vec");
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Another Augur can be sacrificed in response during the same upkeep")
    void canActivateWithAnotherAbilityOnTheStack() {
        addCreatureReady(player1, new AugurIlVec());
        addCreatureReady(player1, new AugurIlVec());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Augur il-Vec");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passBothPriorities();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated outside its controller's upkeep")
    void sacrificeAbilityRequiresYourUpkeep() {
        addCreatureReady(player1, new AugurIlVec());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated during an opponent's upkeep")
    void sacrificeAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        addCreatureReady(player1, new AugurIlVec());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking it")
    void shadowCreatureCannotBeBlockedByNonShadowCreature() {
        addCreatureReady(player1, new AugurIlVec());
        Permanent blocker = addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> declareBlock(blocker, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Shadow prevents it from blocking a non-shadow creature")
    void shadowCreatureCannotBlockNonShadowCreature() {
        Permanent attacker = addCreatureReady(player1, new BlindPhantasm());
        Permanent blocker = addCreatureReady(player2, new AugurIlVec());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> declareBlock(blocker, gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Shadow creatures can block each other")
    void shadowCreatureCanBeBlockedByAnotherShadowCreature() {
        Permanent attacker = addCreatureReady(player1, new SpiritEnDal());
        Permanent blocker = addCreatureReady(player2, new AugurIlVec());

        declareAttackersAndPrepareBlockers(List.of(0));
        declareBlock(blocker, gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, int attackerIndex) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
