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
