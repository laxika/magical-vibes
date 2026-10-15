package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatcherOfTheRoost.class, MasterOfPearls.class, SaguArcher.class})
class WatcherOfTheRoostTest extends BaseCardTest {

    @Test
    void castingFaceDownDoesNotRequireAWhiteCard() {
        harness.setHand(player1, List.of(new WatcherOfTheRoost()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Watcher of the Roost").isFaceDown()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void morphCannotBePaidByANonWhiteCard() {
        Permanent permanent = addFaceDownWatcher();
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(permanent.isFaceDown()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void turningFaceUpRevealsWhiteCardWithoutManaAndGainsTwoLife() {
        Permanent permanent = addFaceDownWatcher();
        MasterOfPearls whiteCard = new MasterOfPearls();
        harness.setHand(player1, List.of(whiteCard));
        harness.setLife(player1, 17);

        harness.turnFaceUp(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(permanent.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void manaCannotReplaceRevealingAWhiteCard() {
        Permanent permanent = addFaceDownWatcher();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(permanent.isFaceDown()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void castingFaceUpDoesNotGainLife() {
        harness.setHand(player1, List.of(new WatcherOfTheRoost()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Watcher of the Roost");
        harness.assertLife(player1, 20);
    }

    private Permanent addFaceDownWatcher() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheRoost());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
