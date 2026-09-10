package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForebodingStatue.class, ForsakenThresher.class})
class ForebodingStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for any color and puts an omen counter on itself")
    void tapsForManaAndAddsOmenCounter() {
        Permanent statue = addReadyStatue(player1);

        harness.activateAbility(player1, indexOf(player1, statue), 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(statue.getCounterCount(CounterType.OMEN)).isEqualTo(1);
        assertThat(statue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps and transforms at the beginning of the end step with three omen counters")
    void transformsAtEndStepWithThreeOmenCounters() {
        Permanent statue = addReadyStatue(player1);
        statue.setCounterCount(CounterType.OMEN, 3);
        statue.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(statue.isTransformed()).isTrue();
        assertThat(statue.getCard()).isInstanceOf(ForsakenThresher.class);
        assertThat(statue.isTapped()).isFalse();
        assertThat(statue.getCounterCount(CounterType.OMEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform at the end step with fewer than three omen counters")
    void doesNotTransformBelowThreeOmenCounters() {
        Permanent statue = addReadyStatue(player1);
        statue.setCounterCount(CounterType.OMEN, 2);
        statue.tap();

        advanceToEndStep(player1);

        assertThat(statue.isTransformed()).isFalse();
        assertThat(statue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forsaken Thresher adds any-color mana at the beginning of its controller's first main phase")
    void backFaceAddsManaAtFirstMainPhase() {
        Permanent thresher = addTransformedStatue(player1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(thresher.isTransformed()).isTrue();
    }

    private Permanent addReadyStatue(Player player) {
        return addReadyPermanent(player, new ForebodingStatue());
    }

    private Permanent addTransformedStatue(Player player) {
        ForebodingStatue card = new ForebodingStatue();
        Permanent statue = new Permanent(card);
        statue.setSummoningSick(false);
        statue.setCard(card.getBackFaceCard());
        statue.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(statue);
        return statue;
    }

    private Permanent addReadyPermanent(Player player, ForebodingStatue card) {
        Permanent statue = new Permanent(card);
        statue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(statue);
        return statue;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
