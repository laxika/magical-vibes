package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogBadger;
import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChokingMiasma.class, BogBadger.class, YavimayaIconoclast.class})
class ChokingMiasmaTest extends BaseCardTest {

    @Test
    void withoutKickerGivesAllCreaturesMinusTwoMinusTwo() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BogBadger());
        cast(false);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    void kickedSpellChoosesAControlledCreatureForTheCounter() {
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());
        Permanent otherControlledCreature = harness.addToBattlefieldAndReturn(player1, new BogBadger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BogBadger());
        prepareSpell(true);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosenCreature.getId(), otherControlledCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenCreature.getId()));
        harness.passBothPriorities();

        assertThat(chosenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, chosenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherControlledCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    void creatureDebuffWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BogBadger());
        cast(false);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private void cast(boolean kicked) {
        prepareSpell(kicked);
        if (kicked) {
            harness.castKickedSorcery(player1, 0);
        } else {
            harness.castAndResolveSorcery(player1, 0, 0);
            return;
        }
        harness.passBothPriorities();
    }

    @Test
    void kickedSpellStillDebuffsOpponentsWhenControllerHasNoCreatures() {
        harness.addToBattlefield(player2, new YavimayaIconoclast());

        cast(true);

        harness.assertNotOnBattlefield(player2, "Yavimaya Iconoclast");
        harness.assertInGraveyard(player2, "Yavimaya Iconoclast");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedCounterSavesTheOnlyControlledCreatureBeforeTheDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());

        cast(true);

        harness.assertOnBattlefield(player1, "Yavimaya Iconoclast");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void chosenCounterSavesOneCreatureWhileTheOtherDies() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        prepareSpell(true);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen).doesNotContain(other);
        harness.assertInGraveyard(player1, "Yavimaya Iconoclast");
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(1);
    }

    @Test
    void creatureEnteringAfterResolutionDoesNotGetTheDebuff() {
        cast(false);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());

        harness.assertOnBattlefield(player2, "Yavimaya Iconoclast");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void prepareSpell(boolean kicked) {
        harness.setHand(player1, List.of(new ChokingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        if (kicked) {
            harness.addMana(player1, ManaColor.GREEN, 1);
        }
    }
}
