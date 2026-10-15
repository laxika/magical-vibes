package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RemnantOfTheRisingStar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuganDefendsTheTemple.class, RemnantOfTheRisingStar.class, GrizzlyBears.class})
class JuganDefendsTheTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a Human Monk that taps for green mana")
    void chapterICreatesManaMonk() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent monk = findPermanent(player1, "Human Monk");
        monk.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(monk), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II puts counters on up to two target creatures")
    void chapterIIPutsCountersOnTwoCreatures() {
        Permanent saga = addSagaWithLore(1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III transforms into Remnant of the Rising Star")
    void chapterIIITransforms() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent remnant = findPermanent(player1, "Remnant of the Rising Star");
        assertThat(remnant.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Remnant puts paid X counters on the entering creature")
    void remnantPutsCountersOnEnteringCreature() {
        addCreatureReady(player1, new RemnantOfTheRisingStar());
        GrizzlyBears entering = new GrizzlyBears();
        harness.setHand(player1, List.of(entering));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Remnant of the Rising Star")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Remnant gets +5/+5 and trample with five modified creatures")
    void remnantGetsThresholdBonus() {
        Permanent remnant = addCreatureReady(player1, new RemnantOfTheRisingStar());
        for (int i = 0; i < 5; i++) {
            Permanent creature = addCreatureReady(player1, new GrizzlyBears());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        }

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, remnant, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Casting the Saga triggers chapter I immediately")
    void castingSagaCreatesMonk() {
        harness.setHand(player1, List.of(new JuganDefendsTheTemple()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Monk")).isEqualTo(1);
        assertThat(findPermanent(player1, "Human Monk").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Jugan Defends the Temple")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II may choose no targets even when creatures are available")
    void chapterIICanChooseNoTargets() {
        addSagaWithLore(1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter II may choose only one creature controlled by an opponent")
    void chapterIICanChooseOneOpposingCreature() {
        addSagaWithLore(1);
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter II still counters its remaining target if another target leaves")
    void chapterIIResolvesForRemainingTarget() {
        addSagaWithLore(1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning transformed clears counters and does not trigger Remnant for itself")
    void transformationCreatesNewPermanentWithoutSelfTrigger() {
        Permanent saga = addSagaWithLore(2);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent remnant = findPermanent(player1, "Remnant of the Rising Star");
        assertThat(remnant.getId()).isNotEqualTo(saga.getId());
        assertThat(remnant.getCounterCount(CounterType.LORE)).isZero();
        assertThat(remnant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(remnant.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Paying X creates a separate counter trigger before any counters are placed")
    void paidCountersUseSeparateReflexiveTrigger() {
        addCreatureReady(player1, new RemnantOfTheRisingStar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 2);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Remnant can decline to pay X without spending mana or adding counters")
    void remnantCanDeclinePayment() {
        addCreatureReady(player1, new RemnantOfTheRisingStar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A modified Remnant counts itself and loses its bonus below five creatures")
    void thresholdCountsSelfAndUpdatesContinuously() {
        Permanent remnant = addCreatureReady(player1, new RemnantOfTheRisingStar());
        for (int i = 0; i < 4; i++) {
            Permanent creature = addCreatureReady(player1, new GrizzlyBears());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        }
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, remnant, Keyword.TRAMPLE)).isFalse();

        remnant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, remnant, Keyword.TRAMPLE)).isTrue();

        remnant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, remnant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Remnant does not trigger for an opponent's entering creature")
    void remnantIgnoresOpposingCreatureEntry() {
        addCreatureReady(player1, new RemnantOfTheRisingStar());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new JuganDefendsTheTemple());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
