package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WatcherOfTheRoostTest extends BaseCardTest {

    @Test
    void morphRequiresAndRevealsAWhiteCardWithoutRemovingItFromHand() {
        WatcherOfTheRoost watcher = new WatcherOfTheRoost();
        MasterOfPearls whiteCard = new MasterOfPearls();
        harness.setHand(player1, List.of(watcher, whiteCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Watcher of the Roost");
        assertThat(permanent.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard);
    }

    @Test
    void morphCannotBePaidByANonWhiteCard() {
        harness.setHand(player1, List.of(new WatcherOfTheRoost(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be white card");
    }

    @Test
    void turningFaceUpGainsTwoLife() {
        harness.setHand(player1, List.of(new WatcherOfTheRoost(), new MasterOfPearls()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0, 1);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Watcher of the Roost");
        harness.setLife(player1, 17);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        harness.passBothPriorities();

        assertThat(permanent.isFaceDown()).isFalse();
        harness.assertLife(player1, 19);
    }
}
