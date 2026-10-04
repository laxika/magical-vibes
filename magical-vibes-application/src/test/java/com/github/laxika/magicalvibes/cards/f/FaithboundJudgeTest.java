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

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(judgment.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void judgmentDoesNotTriggerDuringEnchantedOpponentsUpkeep() {
        Permanent judgment = transformedJudgmentWithCounters(2);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(judgment.getCounterCount(CounterType.JUDGMENT)).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void judgmentAddsCounterWithoutEndingGameBelowThree() {
        Permanent judgment = transformedJudgmentWithCounters(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(judgment.getCounterCount(CounterType.JUDGMENT)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void judgmentCanEnchantItsControllerAndMakeThemLose() {
        Permanent judgment = transformedJudgmentWithCounters(2);
        judgment.setAttachedTo(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(judgment.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void judgeDoesNotTriggerAtThreeCounters() {
        Permanent judge = addCreatureReady(player1, new FaithboundJudge());
        judge.setCounterCount(CounterType.JUDGMENT, 3);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void judgeRechecksCounterLimitWhenUpkeepAbilityResolves() {
        Permanent judge = addCreatureReady(player1, new FaithboundJudge());
        judge.setCounterCount(CounterType.JUDGMENT, 2);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        judge.setCounterCount(CounterType.JUDGMENT, 3);
        harness.passBothPriorities();

        assertThat(judge.getCounterCount(CounterType.JUDGMENT)).isEqualTo(3);
    }

    @Test
    void judgeDoesNotGainCountersDuringOpponentsUpkeep() {
        Permanent judge = addCreatureReady(player1, new FaithboundJudge());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(judge.getCounterCount(CounterType.JUDGMENT)).isZero();
    }

    @Test
    void disturbCannotBePaidWithFrontFaceManaCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new FaithboundJudge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
    private Permanent transformedJudgmentWithCounters(int counterCount) {
        Permanent judgment = harness.addToBattlefieldAndReturn(player1, new FaithboundJudge());
        judgment.setCard(judgment.getOriginalCard().getBackFaceCard());
        judgment.setTransformed(true);
        judgment.setAttachedTo(player2.getId());
        judgment.setCounterCount(CounterType.JUDGMENT, counterCount);
        return judgment;
    }
}
