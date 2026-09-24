package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoastWatcher;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenGuildInitiate.class, CoastWatcher.class, GoblinBrigand.class})
class RavenGuildInitiateTest extends BaseCardTest {

    @Test
    void turnsFaceUpByReturningABirdToItsOwnersHand() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new CoastWatcher());
        harness.setHand(player1, List.of(new RavenGuildInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent initiate = findPermanent(player1, "Raven Guild Initiate");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(initiate),
                List.of(bird.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(initiate.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bird);
        assertThat(gd.playerHands.get(player1.getId())).contains(bird.getCard());
    }

    @Test
    void cannotTurnFaceUpByReturningANonBird() {
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new RavenGuildInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent initiate = findPermanent(player1, "Raven Guild Initiate");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(initiate), List.of(nonBird.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(initiate.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonBird);
    }

    @Test
    void cannotTurnFaceUpByReturningAnOpponentsBird() {
        Permanent opponentsBird = harness.addToBattlefieldAndReturn(player2, new CoastWatcher());
        harness.setHand(player1, List.of(new RavenGuildInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent initiate = findPermanent(player1, "Raven Guild Initiate");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(initiate),
                List.of(opponentsBird.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(initiate.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentsBird);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(opponentsBird.getCard());
    }
}
