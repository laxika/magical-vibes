package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrivilegedPosition.class, GrayscaledGharial.class, Island.class, LastGasp.class})
class PrivilegedPositionTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target a protected creature")
    void opponentCannotTargetProtectedCreature() {
        harness.addToBattlefield(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grayscaled Gharial");
    }

    @Test
    @DisplayName("Controller can target and destroy their own protected creature")
    void controllerCanTargetProtectedCreature() {
        harness.addToBattlefield(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.assertOnBattlefield(player1, "Privileged Position");
    }

    @Test
    @DisplayName("Two copies protect each other, but the remaining copy loses protection when one leaves")
    void twoCopiesProtectEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));

        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Other permanents you control have hexproof")
    void grantsHexproofToOtherPermanentsYouControl() {
        harness.addToBattlefield(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Privileged Position and opponents' permanents do not gain hexproof")
    void excludesSourceAndOpponentsPermanents() {
        Permanent position = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());

        assertThat(gqs.hasKeyword(gd, position, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Permanents lose granted hexproof when Privileged Position leaves")
    void removesHexproofWhenSourceLeaves() {
        Permanent position = harness.addToBattlefieldAndReturn(player1, new PrivilegedPosition());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, position));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }
}
