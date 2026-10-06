package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardasVanguard.class, GrizzlyBears.class, LlanowarElves.class, PincherBeetles.class})
class SigardasVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives double strike to any number of creatures with different powers")
    void etbGrantsDoubleStrikeToDistinctPowers() {
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent elves = addReadyCreature(new LlanowarElves());
        Permanent vanguard = castVanguard();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                bears.getId(), elves.getId(), vanguard.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(bears.getId(), elves.getId(), vanguard.getId()));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rejects a selection containing creatures with the same power")
    void rejectsDuplicatePowers() {
        Permanent firstBears = addReadyCreature(new GrizzlyBears());
        Permanent secondBears = addReadyCreature(new GrizzlyBears());
        castVanguard();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Attack trigger grants double strike until end of turn")
    void attackTriggerGrantsUntilEndOfTurn() {
        Permanent vanguard = addReadyCreature(new SigardasVanguard());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent elves = addReadyCreature(new LlanowarElves());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vanguard)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(vanguard.getId(), bears.getId(), elves.getId()));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("May choose no creatures even when creatures are available")
    void mayChooseNoCreatures() {
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent vanguard = castVanguard();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May choose a single opposing creature without targeting it")
    void mayChooseOpposingCreatureWithShroud() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PincherBeetles());
        Permanent vanguard = castVanguard();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(opponent.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId()));

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Uses effective power at resolution rather than printed power")
    void creaturesWithSamePrintedPowerMayHaveDifferentEffectivePowers() {
        Permanent first = addReadyCreature(new GrizzlyBears());
        Permanent second = addReadyCreature(new GrizzlyBears());
        castVanguard();
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rejects different printed powers that become equal before resolution")
    void rejectsEqualEffectivePowersAndAllowsRetry() {
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent elves = addReadyCreature(new LlanowarElves());
        castVanguard();
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(bears.getId(), elves.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.handleMultiplePermanentsChosen(player1, List.of(elves.getId()));

        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows entering during the opponent's combat")
    void flashDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SigardasVanguard(), "{4}{W}");
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Sigarda's Vanguard");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(vanguard.getId()));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent castVanguard() {
        harness.castFromHand(player1, new SigardasVanguard(), "{4}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Sigarda's Vanguard");
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
