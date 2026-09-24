package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetedJewel.class, GrizzlyBears.class})
class CovetedJewelTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it enters")
    void drawsThreeCardsOnEntry() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new CovetedJewel()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Adds three mana of a chosen color")
    void addsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new CovetedJewel());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        Permanent jewel = findPermanent(player1, "Coveted Jewel");
        assertThat(jewel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Makes the attacking player draw three cards and gain the Jewel after an unblocked attack")
    void transfersToPlayerWithUnblockedCreature() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new CovetedJewel());
        jewel.tap();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player2, new GrizzlyBears());
        int startingHandSize = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(startingHandSize + 3);
        harness.assertNotOnBattlefield(player1, "Coveted Jewel");
        harness.assertOnBattlefield(player2, "Coveted Jewel");
        assertThat(jewel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when the attacking creature is blocked")
    void doesNotTriggerWhenBlocked() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new CovetedJewel());
        jewel.tap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1,
                List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jewel);
        assertThat(jewel.isTapped()).isTrue();
    }
}
