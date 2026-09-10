package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SinnersJudgment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaithboundJudge.class, SinnersJudgment.class})
class FaithboundJudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds judgment counters through the third counter only")
    void upkeepAddsJudgmentCountersUntilThree() {
        Permanent judge = addCreatureReady(player1, new FaithboundJudge());
        judge.setCounterCount(CounterType.JUDGMENT, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(judge.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(judge.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);
    }

    @Test
    @DisplayName("Three judgment counters let Faithbound Judge attack despite defender")
    void attacksWithThreeJudgmentCounters() {
        Permanent judge = addCreatureReady(player1, new FaithboundJudge());
        judge.setCounterCount(CounterType.JUDGMENT, 2);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        judge.setCounterCount(CounterType.JUDGMENT, 3);

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Disturb casts Sinner's Judgment transformed and exiles it from the graveyard")
    void disturbEntersAsSinnersJudgment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new FaithboundJudge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent judgment = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(judgment.isTransformed()).isTrue();
        assertThat(judgment.getCard()).isInstanceOf(SinnersJudgment.class);
        assertThat(judgment.getAttachedTo()).isEqualTo(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, judgment));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(judgment.getOriginalCard().getId());
    }

    @Test
    @DisplayName("Sinner's Judgment makes its enchanted player lose after three counters")
    void enchantedPlayerLosesAtThreeCounters() {
        Permanent judgment = transformedJudgmentWithCounters(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(judgment.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent transformedJudgmentWithCounters(int counterCount) {
        Permanent judgment = new Permanent(new FaithboundJudge());
        judgment.setCard(judgment.getOriginalCard().getBackFaceCard());
        judgment.setTransformed(true);
        judgment.setAttachedTo(player2.getId());
        judgment.setCounterCount(CounterType.JUDGMENT, counterCount);
        gd.playerBattlefields.get(player1.getId()).add(judgment);
        return judgment;
    }
}
