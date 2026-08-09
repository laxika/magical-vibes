package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PursuitOfKnowledgeTest extends BaseCardTest {

    @Test
    void mayReplaceDrawWithStudyCounter() {
        Permanent pursuit = addPursuit();
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    void decliningReplacementDrawsCard() {
        Permanent pursuit = addPursuit();
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void removesCountersSacrificesAndDrawsSeven() {
        Permanent pursuit = addPursuit();
        pursuit.setCounterCount(CounterType.STUDY, 3);
        List<Card> cards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, cards);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pursuit of Knowledge");
        harness.assertInGraveyard(player1, "Pursuit of Knowledge");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void requiresThreeStudyCountersToActivate() {
        Permanent pursuit = addPursuit();
        pursuit.setCounterCount(CounterType.STUDY, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addPursuit() {
        Permanent pursuit = new Permanent(new PursuitOfKnowledge());
        pursuit.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pursuit);
        return pursuit;
    }
}
