package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DramaticReversalTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all nonland permanents you control")
    void untapsAllNonlandPermanentsYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent bear = battlefield.get(0);
        Permanent mindStone = battlefield.get(1);
        Permanent island = battlefield.get(2);
        bear.tap();
        mindStone.tap();
        island.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(mindStone.isTapped()).isFalse();
        assertThat(island.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }
}
