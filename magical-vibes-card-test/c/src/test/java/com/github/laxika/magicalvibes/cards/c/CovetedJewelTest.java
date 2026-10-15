package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetedJewel.class, GrizzlyBears.class})
class CovetedJewelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws three cards")
    void enteringDrawsThreeCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new CovetedJewel());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Tapping adds three mana of one chosen color")
    void tappingAddsThreeMana() {
        harness.addToBattlefield(player1, new CovetedJewel());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("One or more unblocked attackers make their controller draw three and gain the Jewel")
    void unblockedAttackTransfersJewelOnce() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player2, new CovetedJewel());
        jewel.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Coveted Jewel");
        assertThat(jewel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A fully blocked attack does not transfer the Jewel")
    void blockedAttackDoesNotTrigger() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player2, new CovetedJewel());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(jewel);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jewel);
    }

    @Test
    @DisplayName("A partially blocked attack transfers the Jewel")
    void partiallyBlockedAttackTransfersJewel() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player2, new CovetedJewel());
        jewel.tap();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jewel);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jewel);
        assertThat(jewel.isTapped()).isFalse();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jewel), null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(jewel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each Jewel triggers once even when several attackers are unblocked")
    void eachJewelTriggersOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CovetedJewel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CovetedJewel());
        first.tap();
        second.tap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }
}
