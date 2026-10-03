package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Demoralize.class, DuskImp.class})
class DemoralizeTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain menace until end of turn")
    void allCreaturesGainMenace() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());

        castDemoralize();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(opposingCreature.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Threshold prevents all creatures from blocking")
    void thresholdPreventsAllCreaturesFromBlocking() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(bls.canBlockAttacker(gd, ownCreature, opposingCreature,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("A creature entering after threshold resolves still can't block this turn")
    void creatureEnteringAfterThresholdResolvesCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Fewer than seven cards in the caster's graveyard do not enable threshold")
    void thresholdDoesNotApplyBelowSevenCards() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player2, new DuskImp());
        castDemoralize();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Menace permits two blockers without threshold")
    void menacePermitsTwoBlockers() {
        addCreatureReady(player1, new DuskImp());
        Permanent firstBlocker = addCreatureReady(player2, new DuskImp());
        Permanent secondBlocker = addCreatureReady(player2, new DuskImp());
        castDemoralize();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain menace")
    void laterCreatureDoesNotGainMenace() {
        castDemoralize();
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Threshold checks the graveyard at resolution and persists after it empties")
    void thresholdUsesResolutionGraveyardAndPersists() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        harness.castFromHand(player1, new Demoralize(), "{2}{R}");
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Losing threshold before resolution leaves creatures able to block")
    void thresholdLostBeforeResolutionDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));
        harness.castFromHand(player1, new Demoralize(), "{2}{R}");
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Menace and the threshold blocking restriction expire at end of turn")
    void bothEffectsExpireAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));
        castDemoralize();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.MENACE)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castDemoralize() {
        harness.castFromHand(player1, new Demoralize(), "{2}{R}");
        harness.passBothPriorities();
    }
}
