package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WheelOfFortune;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaktamunLorespinnerWheelOfFortune.class, WheelOfFortune.class, GrizzlyBears.class})
class NaktamunLorespinnerWheelOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared at your upkeep when any player has one or fewer cards in hand")
    void becomesPreparedWhenAnyPlayerHasSmallHand() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isTrue();
        assertThat(lorespinner.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when every player has more than one card in hand")
    void doesNotBecomePreparedWhenAllPlayersHaveLargeHands() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(lorespinner.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not become prepared if the hand condition is false when the trigger resolves")
    void rechecksHandConditionAtResolution() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Casting the prepared Wheel of Fortune copy discards hands, draws seven, and unprepares the creature")
    void castingPreparedWheelOfFortuneResolvesAndUnprepares() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        fillLibraries(7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        UUID copyId = lorespinner.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(lorespinner.isPrepared()).isFalse();
        assertThat(lorespinner.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    private void fillLibraries(int cardsEach) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player2.getId()).clear();
        for (int i = 0; i < cardsEach; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
            gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        }
    }
}
