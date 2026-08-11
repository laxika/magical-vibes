package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysticOfTheHiddenWayTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndBecomesUnblockableWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new MysticOfTheHiddenWay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Mystic of the Hidden Way");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));
        harness.passBothPriorities();

        assertThat(mystic.isFaceDown()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, mystic)).isTrue();
    }
}
