package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShinryuTranscendentRival;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZenosYaeGalvus.class, ShinryuTranscendentRival.class, GrizzlyBears.class})
class ZenosYaeGalvusTest extends BaseCardTest {

    @Test
    void controllerChoosesOpposingCreatureAndDebuffsAllOthers() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent debuffed = addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(chosen.getId(), debuffed.getId());

        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zenos);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosen);
    }

    @Test
    void transformsWhenTheChosenCreatureLeavesTheBattlefield() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chosen));
        harness.passBothPriorities();

        assertThat(zenos.isTransformed()).isTrue();
        assertThat(zenos.getCard()).isInstanceOf(ShinryuTranscendentRival.class);
    }

    @Test
    void debuffsOwnCreaturesEvenWhenNoOpponentCreatureCanBeChosen() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        Permanent zenos = castZenos();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(zenos);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ally.getCard());
        assertThat(zenos.isTransformed()).isFalse();
    }

    @Test
    void enterAbilityStillChoosesAndDebuffsAfterZenosLeaves() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ZenosYaeGalvus(), "{3}{B}{B}");
        harness.passBothPriorities();
        Permanent zenos = findPermanent(player1, "Zenos yae Galvus");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zenos));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ally.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosen);
    }

    @Test
    void unrelatedCreatureLeavingDoesNotTriggerTransform() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();
        Permanent unrelated = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, unrelated));

        assertThat(gd.stack).isEmpty();
        assertThat(zenos.isTransformed()).isFalse();
    }

    @Test
    void debuffAffectsOwnCreaturesAndExpiresAtEndOfTurn() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castZenos();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
    }

    @Test
    void creaturesEnteringAfterTheAbilityResolvesAreNotDebuffed() {
        addCreatureReady(player2, new GrizzlyBears());
        castZenos();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lateCreature);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);
    }

    @Test
    void opponentLosingAfterTransformationEndsTheGameWithControllerWinning() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent zenos = castZenos();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chosen));
        resolveAllTriggers();
        assertThat(zenos.isTransformed()).isTrue();

        harness.setLife(player2, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent castZenos() {
        harness.castFromHand(player1, new ZenosYaeGalvus(), "{3}{B}{B}");
        resolveAllTriggers();
        return findPermanent(player1, "Zenos yae Galvus");
    }
}
