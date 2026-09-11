package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PursuitOfKnowledge.class, YouthfulKnight.class})
class PursuitOfKnowledgeTest extends BaseCardTest {

    @Test
    void mayReplaceDrawWithStudyCounter() {
        Permanent pursuit = addPursuit();
        YouthfulKnight card = new YouthfulKnight();
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
        YouthfulKnight card = new YouthfulKnight();
        harness.setLibrary(player1, List.of(card));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Youthful Knight");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void removesCountersSacrificesAndDrawsSeven() {
        Permanent pursuit = addPursuit();
        pursuit.setCounterCount(CounterType.STUDY, 3);
        List<Card> cards = List.of(
                new YouthfulKnight(), new YouthfulKnight(), new YouthfulKnight(), new YouthfulKnight(),
                new YouthfulKnight(), new YouthfulKnight(), new YouthfulKnight());
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

    @Test
    void onlyControllerCanReplaceTheirDraw() {
        Permanent pursuit = addPursuit();
        YouthfulKnight card = new YouthfulKnight();
        harness.setLibrary(player2, List.of(card));
        int handSize = gd.playerHands.get(player2.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).contains(card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void eachDrawInMultipleDrawInstructionCanBeReplacedIndependently() {
        Permanent pursuit = addPursuit();
        YouthfulKnight first = new YouthfulKnight();
        YouthfulKnight second = new YouthfulKnight();
        harness.setLibrary(player1, List.of(first, second));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void mayReplaceDrawFromEmptyLibrary() {
        Permanent pursuit = addPursuit();
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pursuit.getCounterCount(CounterType.STUDY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private Permanent addPursuit() {
        Permanent pursuit = new Permanent(new PursuitOfKnowledge());
        pursuit.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pursuit);
        return pursuit;
    }
}
