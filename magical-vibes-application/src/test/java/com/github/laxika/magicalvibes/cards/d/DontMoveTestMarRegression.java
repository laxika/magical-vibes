package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DontMove.class, GrizzlyBears.class, Plains.class})
class DontMoveTestMarRegression extends BaseCardTest {

    @Test
    void destroysAllCreaturesThatAreTappedOnResolution() {
        Permanent ownTapped = addCreature(player1);
        ownTapped.tap();
        Permanent opponentTapped = addCreature(player2);
        opponentTapped.tap();
        Permanent ownUntapped = addCreature(player1);
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        tappedLand.tap();

        castDontMove();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownUntapped)
                .doesNotContain(ownTapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tappedLand)
                .doesNotContain(opponentTapped);
    }

    @Test
    void destroysCreaturesWhenTheyBecomeTappedUntilTheNextTurn() {
        castDontMove();
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        tapAndResolve(ownCreature);
        tapAndResolve(opponentCreature);
        tapAndResolve(land);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void triggerExpiresAtTheBeginningOfTheCastersNextTurn() {
        castDontMove();
        Permanent creature = addCreature(player2);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        tapAndCheck(creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castDontMove() {
        harness.setHand(player1, List.of(new DontMove()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
    }

    private void tapAndResolve(Permanent permanent) {
        tapAndCheck(permanent);
        resolveAllTriggers();
    }

    private void tapAndCheck(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UNTAP);
    }
}
