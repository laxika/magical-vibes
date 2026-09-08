package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenGuildInitiate.class, BirdMaiden.class, GrizzlyBears.class})
class RavenGuildInitiateTest extends BaseCardTest {

    @Test
    void turnsFaceUpByReturningABirdToItsOwnersHand() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new BirdMaiden());
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
        harness.passBothPriorities();

        assertThat(initiate.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bird);
        assertThat(gd.playerHands.get(player1.getId())).contains(bird.getCard());
    }

    @Test
    void cannotTurnFaceUpByReturningANonBird() {
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
}
