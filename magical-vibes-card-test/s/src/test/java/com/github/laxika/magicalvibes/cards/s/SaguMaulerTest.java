package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaguMauler.class, ForceAway.class, SaguArcher.class})
class SaguMaulerTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SaguMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent mauler = findPermanent(player1, "Sagu Mauler");
        assertThat(mauler.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int maulerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mauler);
        harness.turnFaceUp(player1, maulerIndex);
        harness.passBothPriorities();

        assertThat(mauler.isFaceDown()).isFalse();
    }

    @Test
    void morphRequiresBlueManaAndDoesNotUseTheStack() {
        Permanent mauler = castFaceDownMauler();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mauler.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(mauler.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCannotTargetFaceUpMauler() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new SaguMauler());
        harness.setHand(player2, List.of(new ForceAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mauler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mauler);
    }

    @Test
    void controllerCanTargetFaceUpMauler() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new SaguMauler());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, mauler.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mauler);
        assertThat(gd.playerHands.get(player1.getId())).contains(mauler.getCard());
    }

    @Test
    void turningFaceUpInResponseMakesOpponentsTargetIllegal() {
        Permanent mauler = castFaceDownMauler();
        harness.setHand(player2, List.of(new ForceAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, mauler.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, 0);
        assertThat(mauler.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mauler);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof ForceAway);
    }

    @Test
    void faceDownMaulerCanBeReturnedByOpponent() {
        Permanent mauler = castFaceDownMauler();
        harness.setHand(player2, List.of(new ForceAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, mauler.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mauler);
        assertThat(gd.playerHands.get(player1.getId())).contains(mauler.getCard());
    }

    @Test
    void faceUpMaulerTramplesOverBlocker() {
        Permanent mauler = addCreatureReady(player1, new SaguMauler());
        Permanent archer = addCreatureReady(player2, new SaguArcher());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(archer);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mauler);
    }

    @Test
    void faceDownMaulerHasNoTrampleAndDealsOnlyTwoDamage() {
        Permanent mauler = castFaceDownMauler();
        mauler.setSummoningSick(false);
        Permanent archer = addCreatureReady(player2, new SaguArcher());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mauler);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(archer);
        assertThat(archer.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent castFaceDownMauler() {
        harness.setHand(player1, List.of(new SaguMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Sagu Mauler");
    }
}
