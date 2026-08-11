package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterOfPearlsTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndBoostsOwnCreaturesWhenTurnedFaceUp() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MasterOfPearls()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent master = findPermanent(player1, "Master of Pearls");
        assertThat(master.isFaceDown()).isTrue();
        assertThat(master.getEffectivePower()).isEqualTo(2);
        assertThat(master.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.WHITE, 5);
        int masterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(master);
        harness.turnFaceUp(player1, masterIndex);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(4);
        assertThat(master.isFaceDown()).isFalse();
        assertThat(master.getEffectivePower()).isEqualTo(4);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }
}
