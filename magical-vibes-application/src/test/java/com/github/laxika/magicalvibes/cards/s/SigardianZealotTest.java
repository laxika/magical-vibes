package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardianZealot.class, GrizzlyBears.class, LlanowarElves.class})
class SigardianZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat boosts and grants vigilance to creatures with different powers")
    void boostsChosenCreaturesByZealotsPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new SigardianZealot());

        advanceToCombat(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                bears.getId(), elves.getId(), zealot.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(bears.getId(), elves.getId(), zealot.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Rejects a selection containing creatures with the same power")
    void rejectsDuplicatePowers() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SigardianZealot());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SigardianZealot());

        advanceToCombat(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Can choose an opponent's creature")
    void boostsOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new SigardianZealot());

        advanceToCombat(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Can choose no creatures")
    void canChooseNoCreatures() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new SigardianZealot());

        advanceToCombat(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new SigardianZealot());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.VIGILANCE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
