package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViashinoSandswimmer.class)
class ViashinoSandswimmerTest extends BaseCardTest {

    @Test
    void coinFlipReturnsItToHandOrSacrificesIt() {
        addCreatureReady(player1, new ViashinoSandswimmer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        boolean wonFlip = gameLogContains("wins the coin flip for Viashino Sandswimmer");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        if (wonFlip) {
            harness.assertInHand(player1, "Viashino Sandswimmer");
            harness.assertNotInGraveyard(player1, "Viashino Sandswimmer");
        } else {
            harness.assertInGraveyard(player1, "Viashino Sandswimmer");
            harness.assertNotInHand(player1, "Viashino Sandswimmer");
        }
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sandswimmer = harness.addToBattlefieldAndReturn(player1, new ViashinoSandswimmer());
        sandswimmer.setSummoningSick(true);
        sandswimmer.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        if (gameLogContains("wins the coin flip for Viashino Sandswimmer")) {
            harness.assertInHand(player1, "Viashino Sandswimmer");
            harness.assertNotInGraveyard(player1, "Viashino Sandswimmer");
        } else {
            harness.assertInGraveyard(player1, "Viashino Sandswimmer");
            harness.assertNotInHand(player1, "Viashino Sandswimmer");
        }
    }

    @Test
    void stackedActivationsStillFlipAfterSourceLeavesAndDoNotAffectAnotherCopy() {
        ViashinoSandswimmer source = new ViashinoSandswimmer();
        addCreatureReady(player1, source);
        Permanent other = addCreatureReady(player1, new ViashinoSandswimmer());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("the coin flip for Viashino Sandswimmer")))
                .hasSize(2);
        long sourceInHand = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card == source).count();
        long sourceInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card == source).count();
        assertThat(sourceInHand + sourceInGraveyard).isEqualTo(1);
    }
}
