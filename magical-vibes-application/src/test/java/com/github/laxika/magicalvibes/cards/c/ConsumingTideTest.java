package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingTide.class, GrizzlyBears.class, Island.class})
class ConsumingTideTest extends BaseCardTest {

    @Test
    void eachPlayerKeepsOneNonlandPermanentAndTheRestReturnToHand() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Returned = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Returned = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island player2HandCard = new Island();

        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of(player2HandCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player1Kept.getId(), player1Returned.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Kept.getId(), player2Returned.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept, player1Land)
                .doesNotContain(player1Returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept)
                .doesNotContain(player2Returned);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Returned.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2HandCard, player2Returned.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenNoOpponentHasMoreCardsInHand() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Returned = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Returned = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept)
                .doesNotContain(player1Returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept)
                .doesNotContain(player2Returned);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Returned.getCard());
    }
}
