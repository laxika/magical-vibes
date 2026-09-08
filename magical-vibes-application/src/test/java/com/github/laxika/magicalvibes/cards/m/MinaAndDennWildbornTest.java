package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinaAndDennWildborn.class, Forest.class, GrizzlyBears.class})
class MinaAndDennWildbornTest extends BaseCardTest {

    @Test
    @DisplayName("Lets its controller play one additional land each turn")
    void grantsAdditionalLandPlay() {
        harness.addToBattlefield(player1, new MinaAndDennWildborn());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a land and gives target creature trample until end of turn")
    void returnsLandAndGrantsTrample() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new MinaAndDennWildborn());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest.getCard());
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Requires a land to pay the activation cost")
    void requiresLandToReturn() {
        harness.addToBattlefield(player1, new MinaAndDennWildborn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
