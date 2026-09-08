package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridRaptor.class, ZombieGoliath.class, Forest.class})
class PutridRaptorTest extends BaseCardTest {

    @Test
    void turnsFaceUpByDiscardingAZombie() {
        Forest forest = new Forest();
        ZombieGoliath zombie = new ZombieGoliath();
        harness.setHand(player1, List.of(new PutridRaptor(), forest, zombie));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 1);
        harness.passBothPriorities();

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zombie);
    }

    @Test
    void cannotTurnFaceUpByDiscardingANonZombie() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new PutridRaptor(), forest));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor), 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(raptor.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
